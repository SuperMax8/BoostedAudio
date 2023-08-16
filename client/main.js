const servers = {
    iceServers: [
        {
            urls: ['stun:stun1.l.google.com:19302', 'stun:stun2.l.google.com:19302'],
        },
    ],
    iceCandidatePoolSize: 10,
};


const peerConnections = new Map();
let micStream;
let playerId;
const ws = new WebSocket("ws://pyritemc.fr:8081/");
const params = new URLSearchParams(window.location.search);
const localMediaStream = new MediaStream();

// HTML elements
const callButton = document.getElementById('callButton');
const remoteAudio = document.getElementById('remoteAudio');
const users = document.getElementById('users');
const microphoneMaster = document.getElementById('microphoneMaster');
const microphoneNoise = document.getElementById('microphoneNoise');
const microphoneSelect = document.getElementById('microphoneSelect');

let userTemplate;


ws.onopen = async () => {
    console.log("WebSocket Open")

    if (!params.has("token") || !params.has("playerId")) {
        console.log("Without token or playerId this will not connected to the websocket")
        return
    }

    playerId = params.get("playerId")

    sendTrustPacket()
    await setupAudioSystem()

    // Setup html user element
    const user = document.getElementById("user")
    userTemplate = user.outerHTML
    user.remove()


    ws.onmessage = message => {
        handlePackets(message.data)
    }

    setInterval(() => {
        // users.insertAdjacentElement("beforeend", htmlStringToElement(userTemplate));
        console.log("States:")
        peerConnections.forEach((pc, k) => {
            console.log("- State: " + pc.pc.connectionState)
        })
    }, 3000)

}

function htmlStringToElement(htmlString) {
    const tempContainer = document.createElement("div");
    tempContainer.innerHTML = htmlString;
    return tempContainer.firstElementChild;
}

async function handlePackets(packetList) {
    const jsonArray = JSON.parse(packetList);

    for (const packetObject of jsonArray) {
        const type = packetObject.type;
        const value = packetObject.value;
        await handlePacket(type, value)
    }

}

async function handlePacket(type, packet) {
    /*    console.log("----------RECEIVING PACKET----------")
        console.log(JSON.stringify(packet))
        console.log("------------------------------------")*/
    let from;
    let to;
    let pc
    switch (type) {
        case "AddPeerPacket":
            from = packet.from;
            to = packet.to;
            switch (packet.rtcDesc.type) {
                // Packet action from server
                case "createoffer":
                    pc = createPeerConnection(from)
                    const offer = await pc.createOffer()
                    await pc.setLocalDescription(offer)

                    const offerPacket = [
                        {
                            "type": "AddPeerPacket",
                            "value": {
                                "from": playerId,
                                "to": from,
                                "rtcDesc": {
                                    "type": offer.type,
                                    "sdp": offer.sdp,
                                }
                            }
                        }
                    ]
                    /*console.log("Offer: " + JSON.stringify(offerPacket))*/

                    /*console.log("Creating offer")*/
                    ws.send(JSON.stringify(offerPacket))
                    break
                // Packet action from other user, check by server
                // Receive offer
                case "offer":
                    pc = await createPeerConnection(from)
                    const desc = new RTCSessionDescription(packet.rtcDesc);
                    await pc.setRemoteDescription(desc)

                    const answer = await pc.createAnswer()
                    await pc.setLocalDescription(answer)

                    const answerPacket = [
                        {
                            "type": "AddPeerPacket",
                            "value": {
                                "from": to,
                                "to": from,
                                "rtcDesc": {
                                    "type": answer.type,
                                    "sdp": answer.sdp,
                                }
                            }
                        }
                    ]

                    ws.send(JSON.stringify(answerPacket))
                    /*console.log("Receive OFFER, Sending answer")*/
                    break
                // Packet action from other user, check by server
                // Receive answer
                case "answer":
                    pc = peerConnections.get(from).pc
                    await pc.setRemoteDescription(new RTCSessionDescription(packet.rtcDesc))
                    /*console.log("Receive answer")*/
                    break
            }
            break
        case "RTCIcePacket":
            from = packet.from;
            to = packet.to;
            pc = peerConnections.get(from).pc
            const iceCandidate = new RTCIceCandidate({
                "candidate": packet.candidate.candidate,
                "sdpMid": packet.candidate.sdpMid,
                "sdpMLineIndex": packet.candidate.sdpMLineIndex
            });

            const candidate = new RTCIceCandidate(iceCandidate);

            pc.addIceCandidate(candidate)
                .catch((error) => {
                    console.error("Erreur with ICE candidate set : ", error);
                });

            /*console.log("ICE Candidate set")*/
            break
    }
}

function sendTrustPacket() {
    const trustPacket = [{
        "type": "TrustPacket",
        "value": {
            "token": params.get("token")
        }
    }]

    ws.send(JSON.stringify(trustPacket))
}


function createPeerConnection(from) {
    let pc = new RTCPeerConnection(servers)
    if (peerConnections.has(from)) closePeerConnection(peerConnections.get(from))

    const tracks = [];

    const connection = {
        pc: pc,
        tracks: tracks
    }
    peerConnections.set(from, connection)

    pc.ontrack = event => {
        if (event.track.kind === 'audio') {
            const track = event.track;
            localMediaStream.addTrack(track)
            tracks.push(track)
        }
    };

    pc.onicecandidate = (event) => {
        if (event.candidate) {
            let to = null;
            peerConnections.forEach((pcc, id) => {
                if (pcc.pc === pc) to = id
            })
            const packet = [
                {
                    "type": "RTCIcePacket",
                    "value": {
                        "from": playerId,
                        "to": to,
                        type: 'candidate',
                        candidate: event.candidate
                    }
                }
            ]

            ws.send(JSON.stringify(packet));
            // console.log("Sending ICE candidate")
        }
    };

    micStream.getTracks().forEach((track) => {
        pc.addTrack(track, micStream);
    });

    return pc;
}

function closePeerConnection(peerConnectionContainer) {
    for (const track of peerConnectionContainer.tracks) {
        localMediaStream.removeTrack(track)
    }
    peerConnectionContainer.pc.close()
}

async function populateMicrophoneOptions() {
    try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        const audioInputDevices = devices.filter(device => device.kind === "audioinput");

        microphoneSelect.innerHTML = ""; // Clear previous options

        audioInputDevices.forEach(device => {
            const option = document.createElement("option");
            option.value = device.deviceId;
            option.textContent = device.label || `Microphone ${microphoneSelect.childElementCount + 1}`;
            microphoneSelect.appendChild(option);
        });
    } catch (error) {
        console.error("Error populating microphone options:", error);
    }
}

async function setupAudioSystem() {
    remoteAudio.srcObject = localMediaStream;

    document.body.style.setProperty('--volume', 0.5);
    microphoneMaster.addEventListener('input', function () {
        const volume = parseFloat(this.value) / 100; // Convertir la valeur en pourcentage en décimal
        document.body.style.setProperty('--volume', volume);
    });

    micStream = await navigator.mediaDevices.getUserMedia({audio: true});

    await populateMicrophoneOptions(); // Populate the options initially

    microphoneSelect.addEventListener("change", async () => {
        try {
            const selectedDeviceId = microphoneSelect.value;
            const constraints = {audio: {deviceId: selectedDeviceId}};
            micStream = await navigator.mediaDevices.getUserMedia(constraints);
        } catch (error) {
            console.error("Error getting selected microphone stream:", error);
        }
    });
}