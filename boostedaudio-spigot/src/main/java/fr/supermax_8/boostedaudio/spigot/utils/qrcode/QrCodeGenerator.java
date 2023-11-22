package fr.supermax_8.boostedaudio.spigot.utils.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import fr.supermax_8.boostedaudio.api.BoostedAudioAPI;
import fr.supermax_8.boostedaudio.spigot.BoostedAudioSpigot;
import fr.supermax_8.boostedaudio.spigot.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class QrCodeGenerator {

    private static BufferedImage qrCodeOverlay;

    static {
        try {
            File qrCodeOverlayFile = new File(BoostedAudioSpigot.getInstance().getDataFolder(), "qrcodeoverlay.png");
            if (qrCodeOverlayFile.exists()) {
                qrCodeOverlay = ImageIO.read(qrCodeOverlayFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMap(String data, Player p) {
        CompletableFuture.runAsync(() -> {
            ItemStack itm = generateQRcodeMap(data, p.getWorld());
            ItemUtils.sendHandPacket(p, itm);
            p.sendMap(((MapMeta) itm.getItemMeta()).getMapView());
        });
    }

    public static ItemStack generateQRcodeMap(String data, World world) {
        return generateQRcodeMap(generateQRcode(data), world);
    }

    public static ItemStack generateQRcodeMap(BufferedImage image, World world) {
        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(world);
        mapView.setScale(MapView.Scale.CLOSEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        mapMeta.addItemFlags(ItemFlag.values());
        mapMeta.setDisplayName(BoostedAudioAPI.getAPI().getConfiguration().getQrCodeTitle());

        mapView.addRenderer(new MapRenderer() {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                mapCanvas.drawImage(0, 0, image);
            }
        });

        mapMeta.setMapView(mapView);
        itemStack.setItemMeta(mapMeta);

        return itemStack;
    }

    public static BufferedImage generateQRcode(String data) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, 128, 128);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            if (qrCodeOverlay != null) {
                Graphics2D g2d = image.createGraphics();
                g2d.drawImage(qrCodeOverlay, 0, 0, null);
                g2d.dispose();
            }

            return image;
        } catch (Exception e) {
            return null;
        }
    }

}