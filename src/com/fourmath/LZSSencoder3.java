
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class LZSSencoder3 {
	
	public static void main(String[] args) {
		File file = new File("File2.html");
		byte[] bFile = ContentToByteArray.readContentIntoByteArray(file);
		ArrayList<Byte> readBuffer = new ArrayList<Byte>();
		int maxSize = 32767;   
		int lengthToNext = 0;
		int prev = 0;
		
		SingleLinkedList encodedList = new SingleLinkedList();
		encodedList.insertAtEnd((byte) 0);
		LNode ptr = encodedList.head;
		int indexOfLastPointer = 0;
		for (int i = 0; i < bFile.length; i++) {
			int longestMatch = 0;
			int pointer = 0;
			for (int j = 0; j < readBuffer.size(); j++) {
				if (readBuffer.get(j) == bFile[i]) {
					int matchLength = 1;
					while ((j + matchLength < readBuffer.size()) && (i + matchLength < bFile.length) && (readBuffer.get(j + matchLength) == bFile[i + matchLength])) {
						matchLength++;
					}
					if (longestMatch < matchLength) {
						longestMatch = matchLength;
						pointer = j;
					}
				}
			}
			if (longestMatch > 3) {
				if (readBuffer.size() < maxSize) {
					for (int k = i; k < i + longestMatch; k++) {
						if (readBuffer.size() < maxSize) {   
							readBuffer.add(bFile[k]);
						}
					}
				}
				setLink(encodedList, pointer, longestMatch);
				lengthToNext = i - prev;
				setPointer(encodedList, ptr, lengthToNext);
				ptr = encodedList.tail;
				indexOfLastPointer = encodedList.size - 1;
				i = i + longestMatch - 1;
				prev = i + 1;
			}
			else {
				encodedList.insertAtEnd(bFile[i]);
				if (readBuffer.size() < maxSize) {
					readBuffer.add(bFile[i]);
				}
			}
		}
		setPointer(encodedList, ptr, encodedList.size - indexOfLastPointer - 1);
		byte[] byteArray = new byte[encodedList.size - 1];
		ptr = encodedList.head.next;
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = ptr.bt;
			ptr = ptr.next;
		}
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream("temp.html"))) {
            out.write(byteArray);
        }
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	public static void setLink(SingleLinkedList encodedList, int jump, int matchLength) {
		if (jump < 4096 && matchLength > 3 && matchLength < 12) {
			byte byte1 = (byte) (jump >>> 5);
			byte byte2 = (byte) ((byte) ((jump << 27) >>> 24) | (byte) (matchLength - 4));
			encodedList.insertAtEnd(byte1);
			encodedList.insertAtEnd(byte2);
		}
		else {
			byte byte1 = (byte) ((byte) ((jump << 17) >>> 25) | (byte) (-128));
			byte byte2 = (byte) ((jump << 24) >>> 24);
			byte byte3 = (byte) (matchLength - 4);   
			encodedList.insertAtEnd(byte1);
			encodedList.insertAtEnd(byte2);
			encodedList.insertAtEnd(byte3);
		}
	}
	
	public static void setPointer(SingleLinkedList encodedList, LNode ptr, int lengthToNext) {
		if (lengthToNext < 128) {
			ptr.setNext(new LNode((byte) lengthToNext, ptr.next));
			encodedList.size++;
		}
		else {
			byte byte1 = (byte) ((byte) ((lengthToNext << 17) >>> 25) | (byte) (-128));
			byte byte2 = (byte) lengthToNext;
			ptr.setNext(new LNode(byte2, ptr.next));
			ptr.setNext(new LNode(byte1, ptr.next));
			encodedList.size += 2;
		}
	}
	
}