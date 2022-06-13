package com.Ding.threemeal.Adapter;

public class SchoolSearchEntry {
	private String orgName; // 학교 이름
	private String zipAdres; // 학교 주소
	private String orgCode; // 학교 코드
	private String kndScCode; // 학교 분류
	private String crseScCode; // 학교 종류
	private String districtAdres; // 홈페이지 주소

	public SchoolSearchEntry(String districtAdres, String orgName, String zipAdres, String orgCode, String kndScCode, String crseScCode) {
		this.orgName = orgName;
		this.zipAdres = zipAdres;
		this.orgCode = orgCode;
		this.kndScCode = kndScCode;
		this.crseScCode = crseScCode;
		this.districtAdres = districtAdres;
	}

	
	public String getDistrictAdres() {
		return districtAdres;
	}


	public void setDistrictAdres(String districtAdres) {
		this.districtAdres = districtAdres;
	}


	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getZipAdres() {
		return zipAdres;
	}

	public void setZipAdres(String zipAdres) {
		this.zipAdres = zipAdres;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getKndScCode() {
		return kndScCode;
	}

	public void setKndScCode(String kndScCode) {
		this.kndScCode = kndScCode;
	}

	public String getCrseScCode() {
		return crseScCode;
	}

	public void setCrseScCode(String crseScCode) {
		this.crseScCode = crseScCode;
	}
}
