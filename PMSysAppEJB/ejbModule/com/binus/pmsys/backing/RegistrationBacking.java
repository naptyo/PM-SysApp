package com.binus.pmsys.backing;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import com.binus.pmsys.eao.RegistrationEao;
import com.binus.pmsys.entity.NewPatient;
import com.binus.pmsys.enums.PatientEnum;
import com.binus.pmsys.utils.DateHelper;

@Named
@SessionScoped
public class RegistrationBacking extends BasicBacking {
	private static final long serialVersionUID = 8664948727348463034L;
	
	@EJB
	private transient RegistrationEao registrationService;
	
	private NewPatient patient;
	
	private int year;
	private String month;
	private int day;
	
	private int[] years;
	private List<String> months, contactRelation;
	private int[] days;
	
	private String normalDOB; // DOB in user readable format
	
	public RegistrationBacking() { }

	public NewPatient getPatient() {
		return patient;
	}

	public void setPatient(NewPatient patient) {
		this.patient = patient;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}
	
	public int[] getYears() {
		return years;
	}

	public void setYears(int[] years) {
		this.years = years;
	}

	public List<String> getContactRelation() {
		return contactRelation;
	}

	public void setContactRelation(List<String> contactRelation) {
		this.contactRelation = contactRelation;
	}

	public List<String> getMonths() {
		return months;
	}

	public void setMonths(List<String> months) {
		this.months = months;
	}

	public int[] getDays() {
		return days;
	}

	public void setDays(int[] days) {
		this.days = days;
	}

	public String getNormalDOB() {
		return normalDOB;
	}

	public void setNormalDOB(String normalDOB) {
		this.normalDOB = normalDOB;
	}
	
	public void pageInitialize() {
		if(!getIsPostBack()) {
			patient = new NewPatient();
			generateYearMonthDay();
			generateRelationList();
		}
	}
	
	private void generateYearMonthDay() {
		LocalDate now = LocalDate.now();
		
		DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
		months = new ArrayList<String>();
		months.addAll(Arrays.asList(dfs.getMonths()));
		months.remove("");
		
		years = IntStream.rangeClosed(1900,  LocalDate.now().getYear()).toArray();
		days = IntStream.rangeClosed(1, 31).toArray();
		
		day = now.getDayOfMonth();
		year = now.getYear();
		month = DateHelper.getMonthNamefromInt(now.getMonthValue(), Locale.ENGLISH);
	}
	
	private void generateRelationList() {
		contactRelation = new ArrayList<String>();
		contactRelation.add("Ayah");
		contactRelation.add("Ibu");
		contactRelation.add("Kakak");
		contactRelation.add("Adik");
		contactRelation.add("Kakek");
		contactRelation.add("Nenek");
		contactRelation.add("Paman");
		contactRelation.add("Bibi");
		contactRelation.add("Cucu");
		contactRelation.add("Suami");
		contactRelation.add("Istri");
		contactRelation.add("Saudara");
		contactRelation.add("Anak");
		contactRelation.add("Teman");
	}
	
	private void validateDOB() {
		normalDOB = day + "-" + month + "-" + year;
		String dobString = year + "-" + DateHelper.getMonthfromString(month) + "-" + day;
		Date dobDate = DateHelper.formatStringToDate(dobString, "yyyy-MM-dd");
		
		if(day > DateHelper.findLengthDaysinMonthYear(year, DateHelper.getMonthfromString(month))) {
			messageHandler("Tidak ada hari " + day + "di " + month + " " + year, FacesMessage.SEVERITY_ERROR);
		} else {
			patient.setPatientDOB(DateHelper.formatDateToString(dobDate, "yyyy-MM-dd"));
		}
	}
	
	private NewPatient prepackagePatientData(NewPatient patient) {
		if(patient.getHasBPJS() == PatientEnum.YES_BPJS) {
			patient.setPatientBPJS(patient.getPatientBPJS().replaceAll("\\s+", ""));
			patient.setPatientBPJSTypeID(PatientEnum.getBPJSClassByString(patient.getPatientBPJSType()));
		}
		
		patient.setPatientKTP(patient.getPatientKTP().replaceAll("\\s+", ""));
		patient.setPhoneTypeID(PatientEnum.getPhoneTypeByString(patient.getPhoneType()));		
		
		if(patient.getPhoneTypeID() == PatientEnum.HOMEPHONE) {
			patient.setPhoneNumber(patient.getFrontExtNum() + " " + patient.getPhoneNumber());
		} else {
			patient.setPhoneNumber("+62 " + patient.getPhoneNumber());
		}
		
		patient.setProvinceID(1);
		patient.setKabupatenID(1);
		
		return patient;
	}
	
	public String redirectToReview() {
		validateDOB();
		return "review.xhtml?faces-redirect=true";
	}
	
	public String finalizePatientCreation() {
		registrationService.savePatient(prepackagePatientData(this.patient));
		return "/menu.xhtml?faces-redirect=true";
	}
}
