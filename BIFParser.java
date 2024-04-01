
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BIFParser {
    private final int imageWidth;
    private final int imageHigh;
    private static final int MAGIC_NUMBER_SIZE = 8;
    private static final int VERSION_SIZE = 4;
    private static final int NUM_IMAGES_SIZE = 4;
    private static final int TIMESTAMP_MULTIPLIER_SIZE = 4;
    private static final int RESERVED_SIZE = 44;
    private static final int BIF_INDEX_ENTRY_SIZE = 8;

    public BIFParser(int imageWidth, int imageHigh) {
        this.imageWidth = imageWidth;
        this.imageHigh = imageHigh;
    }

    public void parseBIFFile(File file) throws IOException {
        FileInputStream inputStream = null;
        FileInputStream inputStreamImage = null;
        try {
            inputStream = new FileInputStream(file);
            inputStreamImage = new FileInputStream(file);

            // Read magic number
            byte[] magicNumberBytes = new byte[MAGIC_NUMBER_SIZE];
            inputStream.read(magicNumberBytes);
            // Verify magic number
            if (!isBIFFile(magicNumberBytes)) {
                throw new IOException("Invalid file format");
            }

            // Read version
            byte[] versionBytes = new byte[VERSION_SIZE];
            inputStream.read(versionBytes);
            int version = bytesToInt(versionBytes);

            // Read number of images
            byte[] numImagesBytes = new byte[NUM_IMAGES_SIZE];
            inputStream.read(numImagesBytes);
            int numImages = bytesToInt(numImagesBytes);

            // Read timestamp multiplier
            byte[] timestampMultiplierBytes = new byte[TIMESTAMP_MULTIPLIER_SIZE];
            inputStream.read(timestampMultiplierBytes);
            //One interval of 10000 milliseconds, that is, 10 seconds
            int timestampMultiplier = bytesToInt(timestampMultiplierBytes);

            // Skip reserved bytes
            inputStream.skip(RESERVED_SIZE);

            System.out.println("version:" + version + " numImages:" + numImages + " timestampMultiplier:" + timestampMultiplier);

            //mkdir
            String folder = file.getName().replaceAll("\\..*", "");
            new File(file.getParent(), folder).mkdirs();

            // Read BIF index
            for (int i = 0; i < numImages; i++) {
                byte[] indexEntryBytes = new byte[BIF_INDEX_ENTRY_SIZE];
                inputStream.read(indexEntryBytes);
                int frameTimestamp = bytesToInt(indexEntryBytes, 0);

                int absoluteOffset = bytesToInt(indexEntryBytes, 4);
                byte[] imageData = extractImageData(inputStreamImage, absoluteOffset);
//                //Enter one in five minutes
//                if (i % 30 == 0) {
                saveImage(imageData, folder + File.separator + i + ".jpg");
//                }
            }
            System.out.println("parser success! The output results are saved in the " + folder + " folder");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (inputStreamImage != null) {
                inputStreamImage.close();
            }
        }
    }

    private byte[] extractImageData(FileInputStream inputStream, int absoluteOffset) throws IOException {
        ///bin/ffmpeg -f mp4 -threads 1 -skip_interval 10 -copyts -i file:"xx.mp4" -an -sn -vf
        // "select='eq(pict_type,PICT_TYPE_I)',scale=w=320:h=180" -vsync cfr -r 0.1 -f image2
        // "/config/cache/temp/xx/img_%05d.jpg"
        byte[] imageData = new byte[imageWidth * imageHigh]; // Change the size according to your image size
        // Seek to the absolute offset
        inputStream.getChannel().position(absoluteOffset);
        // Read image data
        inputStream.read(imageData);
        return imageData;
    }

    private void saveImage(byte[] imageData, String fileName) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(bais);
        File output = new File(fileName);
        ImageIO.write(image, "jpg", output);
    }


    private int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, 0);
    }

    private int bytesToInt(byte[] bytes, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (bytes[offset + i] & 0xFF) << (i * 8);
        }
        return value;
    }

    private boolean isBIFFile(byte[] magicNumberBytes) {
        byte[] magicNumber = {(byte) 0x89, 0x42, 0x49, 0x46, 0x0d, 0x0a, 0x1a, 0x0a};
        for (int i = 0; i < MAGIC_NUMBER_SIZE; i++) {
            if (magicNumberBytes[i] != magicNumber[i]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            File bifFile = new File(args[0]);
            int imageWidth = Integer.parseInt(args[1]);
            int imageHigh = Integer.parseInt(args[2]);
            new BIFParser(imageWidth, imageHigh).parseBIFFile(bifFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}