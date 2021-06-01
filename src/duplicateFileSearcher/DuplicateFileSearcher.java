package duplicateFileSearcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class DuplicateFileSearcher {

	private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
	private static boolean isRunning = true;
	private static String arg1;
	private static List<String> deleteOption = Arrays.asList("older", "earlier", "printonly");
	
	private static final int DELETEOPTION_OLDER = 0;
	private static final int DELETEOPTION_EARLIER = 1;
	private static final int DELETEOPTION_VERVOSE = 2;
	
	public static void main(String[] args) throws IOException {

		if(args.length != 1 && args.length != 2) { //TODO : check if entered option correctly
			System.out.println("DuplicateFileSearcher v1.0 usage : ");
			System.out.println("java -jar DuplicateFileSearcher <searchDirectory> [--delOpt=(" + deleteOption.stream().collect(Collectors.joining("|")) + ")]");
			System.out.println("default is deleting older");
			return;
		}
		
		arg1 = args[1].split("=")[1];
		
		new Thread(() -> {
			while(isRunning) {
				 try {
				 	 System.out.println(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		Files.list(Paths.get(args[0])).map(Path::toFile).collect(Collectors.groupingBy(File::length)).entrySet().stream().parallel()
			.filter(e -> e.getValue(). size() > 1).map(Map.Entry<Long, List<File>>::getValue)
			.forEach(DuplicateFileSearcher::checkData);
		
		isRunning  = false;
	}
	
	private static void checkData(List<File> list) {
		
		StringBuilder sb = new StringBuilder("");
	
		for(int i = 0; i < list.size() ; i++) {
			for(int j = i + 1; j < list.size() ; j++) {
				
				File a = list.get(i), b = list.get(j);
				
				if (isDuplicate(a, b, sb)) {
					
					switch(deleteOption.indexOf(arg1)) {
					
					case DELETEOPTION_OLDER:
						if(a.lastModified() > b.lastModified()) {
							sb.append("delete : " + b.getName() + "\n");
							b.delete();
						} else {
							sb.append("delete : " + a.getName() + "\n");
							a.delete();
						}
						break;
					
					case DELETEOPTION_EARLIER:
						if(a.lastModified() > b.lastModified()) {
							sb.append("delete : " + a.getName() + "\n");
							a.delete();
						} else {
							sb.append("delete : " + b.getName() + "\n");
							b.delete();
						}
						break;
						
					case DELETEOPTION_VERVOSE:
						break;
						
					default:
						System.err.println("Undefined delete option! : " + arg1 + ", index : " + deleteOption.indexOf(arg1));
						break;
					}
				}
			}	
		}
		
		queue.offer(sb.toString());
		
	}

	private static boolean isDuplicate(File file1, File file2, StringBuilder sb) {

        byte[] first, second;
        int strLenght = "checking : ".length() + (file1.getName().length() > file2.getName().length() ? file1.getName().length() : file2.getName().length());
        
        sb.append(String.format("%" + strLenght  +"s\n", "checking : " + file1.getName()));
        sb.append(String.format("%" + strLenght  +"s\n", file2.getName()));
        
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
