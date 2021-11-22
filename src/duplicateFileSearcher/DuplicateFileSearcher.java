package duplicateFileSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class DuplicateFileSearcher {

	private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
	private static boolean isRunning = true;
	private static DeleteOption delOption;
	private static ArrayList<FilePair> cacheList = new ArrayList<>();
	
	public static void main(String[] args) throws IOException {

		if(args.length != 1 && args.length != 2) { 
			printUsageAndKill();
		}
		
		if(args.length == 1) {
			delOption = DeleteOption.CACHE_ONLY;
		} else {
			delOption = DeleteOption.get(args[1].split("=")[1]);
			if(delOption == null) printUsageAndKill();
		}
		
		new Thread(() -> {
			while(isRunning) {
				 try {
				 	 System.out.println(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		if(args[0].endsWith("dfsCache.ser")) {
			readCached(args[0]);
			deleteCached();
			isRunning  = false;
			return;
		}
		
		Files.list(Paths.get(args[0])).map(Path::toFile).collect(Collectors.groupingBy(File::length)).entrySet().stream().parallel() // convert to sets that each contains Files that have same length
			.filter(e -> e.getValue(). size() > 1).map(Map.Entry<Long, List<File>>::getValue) // and pick those who have more than one value
			.forEach(DuplicateFileSearcher::checkData);
		
		if(delOption == DeleteOption.CACHE_ONLY) {
			writeCached();
		}
		
		isRunning  = false;
	}
	
	private static void writeCached() {
		
		try {
			File path = new File(new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss").format(new Date()) + File.separator + "dfsCache.ser");
			path.createNewFile();
			ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(path));
			ois.writeObject(cacheList);
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@SuppressWarnings("unchecked")
	private static void readCached(String path) {

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path)));
			cacheList = (ArrayList<FilePair>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void deleteCached() {

		StringBuilder sb = new StringBuilder("");
		for (FilePair fp : cacheList) {

			switch (delOption) {

			case DELETE_OLDER:
				if (fp.f1.lastModified() > fp.f2.lastModified()) {
					fp.f2.delete();
					sb.append("delete : " + fp.f2.getName() + "\n");
				} else {
					fp.f1.delete();
					sb.append("delete : " + fp.f1.getName() + "\n");
				}
				break;

			case DELETE_EARLIER:
				if (fp.f1.lastModified() > fp.f2.lastModified()) {
					fp.f1.delete();
					sb.append("delete : " + fp.f1.getName() + "\n");
				} else {
					fp.f2.delete();
					sb.append("delete : " + fp.f2.getName() + "\n");
				}
				break;
				
			case CACHE_ONLY:
				printUsageAndKill();
				break;
				
			}
		}
		queue.offer(sb.toString());
	}

	private static void printUsageAndKill() {
		System.out.println("DuplicateFileSearcher v1.0 usage : ");
		System.out.println("java -jar DuplicateFileSearcher.jar <searchDirectory> [--delOpt=(" + DeleteOption.getCommandArgsList() + ")]");
		System.out.println("default is deleting older");
		System.exit(1);;
	}
	
	private static void checkData(List<File> list) {
		
		StringBuilder sb = new StringBuilder("");
	
		for(int i = 0; i < list.size() ; i++) {
			for(int j = i + 1; j < list.size() ; j++) {
				
				File a = list.get(i), b = list.get(j);
				
				if (isDuplicate(a, b, sb)) {
					
					switch(delOption) {
					
					case DELETE_OLDER:
						if(a.lastModified() > b.lastModified()) {
							b.delete();
							sb.append("delete : " + b.getName() + "\n");
						} else {
							a.delete();
							sb.append("delete : " + a.getName() + "\n");
						}
						break;
					
					case DELETE_EARLIER:
						if(a.lastModified() > b.lastModified()) {
							a.delete();
							sb.append("delete : " + a.getName() + "\n");
						} else {
							b.delete();
							sb.append("delete : " + b.getName() + "\n");
						}
						break;
						
					case CACHE_ONLY:
						cacheList.add(new FilePair(a, b));
						break;
						
					}
				}
			}	
		}
		
		queue.offer(sb.toString());
		
	}

	private static boolean isDuplicate(File file1, File file2, StringBuilder sb) {

        byte[] first, second;
        
        sb.append("\nchecking :\n");
        sb.append(file1.getName() + "\t" + new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss").format(new Date(file1.lastModified())) + "\n");
        sb.append(file2.getName() + "\t" + new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss").format(new Date(file2.lastModified())) + "\n");
        
		try {
			first = Files.readAllBytes(file1.toPath());
			second = Files.readAllBytes(file2.toPath());
			boolean re = Arrays.equals(first, second);
			sb.append("result : " + re);
			return re;
		} catch (IOException e) {
			e.printStackTrace();
			sb.append("result : " + "false (Exception)\n");
			return false;
		}
	}

}
