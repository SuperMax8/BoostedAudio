const servers = {
    iceServers: [
        {
            urls: ['stun:stun1.l.google.com:19302', 'stun:stun2.l.google.com:19302'],
        },
    ],
    iceCandidatePoolSize: 10,
};


const peerConnections = [];
let micStream = null;
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

    if (!params.has("token")) {
        console.log("Without token this will not connected to the websocket")
        // return
    }

    sendTrustPacket()
    setupAudioSystem()

    const user = document.getElementById("user")
    userTemplate = user.outerHTML
    user.remove()

    ws.onmessage = message => {
        handlePackets(message.data)
    }

    pc.ontrack = event => {
        if (event.track.kind === 'audio') {
            localMediaStream.addTrack(event.track)
        }
    };

    pc.onicecandidate = (event) => {
        if (event.candidate) {
            const packet = [
                {
                    "type": "RTCIcePacket",
                    "value": {
                        type: 'candidate',
                        candidate: event.candidate
                    }
                }
            ]

            ws.send(JSON.stringify(packet));
            console.log("Sending ICE candidate")
        }
    };

    setInterval(() => {
        console.log("RTCPeer State:")
        console.log(pc.connectionState)

        users.insertAdjacentElement("beforeend", htmlStringToElement(userTemplate));
    }, 300)

    callButton.onclick = async () => {
        // Create offer
        const offerDescription = await pc.createOffer();
        offerDescription.type
        await pc.setLocalDescription(offerDescription);

        const offer = {
            sdp: offerDescription.sdp,
            type: offerDescription.type,
        };

        console.log(offer)

        const packet = [
            {
                "type": "RTCSessionDescriptionPacket",
                "value": {
                    sdp: offerDescription.sdp,
                    type: offerDescription.type
                }
            }
        ]

        ws.send(JSON.stringify(packet))
        console.log("Sending Offer")

    }

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
    const packetFinal = packet;
    switch (type) {
        case "RTCSessionDescriptionPacket":
            switch (packet.type) {
                case "offer":
                    await pc.setRemoteDescription(new RTCSessionDescription(packetFinal))
                    const answer = await pc.createAnswer()
                    await pc.setLocalDescription(answer)

                    const packet = [
                        {
                            "type": "RTCSessionDescriptionPacket",
                            "value": {
                                type: answer.type,
                                sdp: answer.sdp,
                            }
                        }
                    ]

                    ws.send(JSON.stringify(packet))
                    console.log("Receive OFFER, Sending answer")
                    break
                case "answer":
                    await pc.setRemoteDescription(new RTCSessionDescription(packetFinal))
                    console.log("Receive answer")
                    break
            }
            break
        case "RTCIcePacket":
            const iceCandidate = new RTCIceCandidate({
                candidate: packetFinal.candidate.candidate,
                sdpMid: packetFinal.candidate.sdpMid,
                sdpMLineIndex: packetFinal.candidate.sdpMLineIndex
            });

            const candidate = new RTCIceCandidate(iceCandidate);

            pc.addIceCandidate(candidate)
                .catch((error) => {
                    console.error("Erreur with ICE candidate set : ", error);
                });

            console.log("ICE Candidate set")
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

    micStream.getTracks().forEach((track) => {
        pc.addTrack(track, micStream);
    });

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