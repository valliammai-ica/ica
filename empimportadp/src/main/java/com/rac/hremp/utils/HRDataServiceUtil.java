package com.rac.hremp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.rac.hremp.objects.Employee;
import com.rac.hremp.objects.EmployeeFullData;
import com.rac.hremp.objects.EmployeeHierarchy;
import com.rac.hremp.objects.EmployeeHierarchyList;

public class HRDataServiceUtil {
	
	public static HashMap<String,Integer> companyMap = new HashMap<String,Integer>();
	
	public static void buildCompanyMap() {
		companyMap.put("TEX", 1);
		companyMap.put("EAS", 1);
		companyMap.put("WES", 1);
		companyMap.put("AES", 1);
		companyMap.put("ATX", 1);
		companyMap.put("AWS", 1);
		companyMap.put("NPS", 1);
		companyMap.put("GIN", 4);
		companyMap.put("MEX", 71);
	}

	public static HashMap<String, Integer> getCompanyMap() {
		return companyMap;
	}

	public static void setCompanyMap(HashMap<String, Integer> companyMap) {
		HRDataServiceUtil.companyMap = companyMap;
	}
}
