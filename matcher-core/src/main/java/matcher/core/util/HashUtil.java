package matcher.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class HashUtil {
	public static byte[] getSHA256(Path path) {
		try {
			byte[] bytes = Files.readAllBytes(path);
			return MessageDigest.getInstance("SHA-256").digest(bytes);
		} catch (IOException | NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static void performAutoShared(List<Path> classPathA, List<Path> classPathB, List<Path> sharedClassPath) {
		HashMap<Path, byte[]> hashingCache = new HashMap<>();

		{
			Iterator<Path> iterator = Stream.concat(classPathA.stream(), classPathB.stream()).iterator();

			while (iterator.hasNext()) {
				Path path = iterator.next();

				if (!hashingCache.containsKey(path)) {
					hashingCache.put(path, getSHA256(path));
				}
			}
		}

		Iterator<Path> iteratorA = classPathA.iterator();

		outer_loop: while (iteratorA.hasNext()) {
			Path pathA = iteratorA.next();
			byte[] hashA = hashingCache.get(pathA);

			if (hashA == null) continue;

			Iterator<Path> iteratorB = classPathB.iterator();

			while (iteratorB.hasNext()) {
				Path pathB = iteratorB.next();
				byte[] hashB = hashingCache.get(pathB);

				if (hashB == null) continue;

				if (Arrays.equals(hashA, hashB)) {
					iteratorA.remove();
					iteratorB.remove();
					sharedClassPath.add(pathA);
					continue outer_loop;
				}
			}
		}
	}
}
