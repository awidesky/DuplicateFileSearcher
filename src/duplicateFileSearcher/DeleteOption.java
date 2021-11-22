package duplicateFileSearcher;

public enum DeleteOption {

	DELETE_OLDER("older"),
	DELETE_EARLIER("earlier"),
	CACHE_ONLY("cacheNprintonly");

	private String commandArg;
	
	private DeleteOption(String string) {
		commandArg = string;
	}
	
	public static String getCommandArgsList() {
		return "older" + "|" + "earlier" + "|" + "cacheNprintonly";
	}
	
	public static DeleteOption get(String s) {
		switch(s) {
		
		case "older":
			return DELETE_OLDER;
		
		case "earlier":
			return DELETE_EARLIER;
			
		case "cacheNprintonly":
			return DELETE_EARLIER;
		
		default:
			return null;
		}
	}
	
	public String getCommandArg() {
		return commandArg;
	}
}
