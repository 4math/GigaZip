
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class LZSSdecoder3 {

	public static void main(String[] args) {
		File file = new File("temp.html");
		byte[] bFile = ContentToByteArray.readContentIntoByteArray(file);
		ArrayList<Byte> decodedFile = new ArrayList<Byte>();
		int originalBytes = 0;
		
		for (int i = 0; i < bFile.length; i++) {
			if (bFile[i] >= 0) {
				originalBytes = bFile[i] & 0xff;
			}
			else {
				int byte1 = bFile[i] & 0xff;
				int byte2 = bFile[++i] & 0xff;
				originalBytes = (((byte1 << 25) >>> 25) << 8) | byte2;
			}
			
			for (int j = 0; j < originalBytes; j++) {
				decodedFile.add(bFile[++i]);
			}
			if (i == bFile.length - 1) {
				break;
			}
			if (bFile[i + 1] >= 0) {
				int byte1 = bFile[++i] & 0xff;
				int byte2 = bFile[++i] & 0xff;
				int jump = ((byte1 << 8) | byte2) >>> 3;
				int matchLength = ((byte2 << 29) >>> 29) + 4;
				for (int j = jump; j < jump + matchLength; j++) {
					decodedFile.add(decodedFile.get(j));
				}
			}
			else {
				int byte1 = bFile[++i] & 0xff;
				int byte2 = bFile[++i] & 0xff;
				int matchLength = (bFile[++i] & 0xff) + 4; 
				int jump = ((((byte1 << 25) >>> 25) << 8) | byte2);
				for (int j = jump; j < jump + matchLength; j++) {
					decodedFile.add(decodedFile.get(j));
				}
			}
		}
		Byte[] tempArray = decodedFile.toArray(Byte[]::new);
		byte[] decodedArray = new byte[tempArray.length];
		for (int i = 0; i < tempArray.length; i++) {
			decodedArray[i] = tempArray[i];
		}
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream("answer.html"))) {
            out.write(decodedArray);
        }
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

}
