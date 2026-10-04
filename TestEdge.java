import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class TestEdge {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File(" crop512.png\));
 int w = img.getWidth();
 int h = img.getHeight();
 System.out.println(\Loaded crop512.png: \ + w + \x\ + h);
 }
}
