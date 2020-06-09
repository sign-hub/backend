package core.services;

public enum Auths {
	
	AT_MANAGE_TESTS("Atlas Manage Tests"),
	AT_PLAY_TESTS("Atlas Play Tests");
	
	private String authName;
	
	private Auths(String authName) {
		this.authName = authName;
	}

	public String getAuthName() {
		return authName;
	}

	public void setAuthName(String authName) {
		this.authName = authName;
	}
	
	
}
