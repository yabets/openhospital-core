/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.patient.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.generaldata.MessageBundle;
import org.isf.patient.model.Patient;
import org.isf.patient.service.PatientIoOperations;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.OHDataValidationException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PatientBrowserManager {
	
	@Autowired
	private PatientIoOperations ioOperations;
	
	@Autowired
	private AdmissionBrowserManager admissionManager;
	
	@Autowired
	private BillBrowserManager billManager;

	protected LinkedHashMap<String, String> maritalHashMap;

	protected LinkedHashMap<String, String> professionHashMap;
	
	public PatientIoOperations getIoOperations() {
		return ioOperations;
	}

	public void setIoOperations(PatientIoOperations ioOperations) {
		this.ioOperations = ioOperations;
	}
	
	/**
	 * method that insert a new Patient in the db
	 * 
	 * @param patient
	 * @return saved / updated patient
	 * @throws OHServiceException when validation failed
	 */
	public Patient savePatient(Patient patient) throws OHServiceException {
        validate(patient);
        return ioOperations.savePatient(patient);
	}
	
	/**
	 * method that returns the full list of Patients not logically deleted
	 * 
	 * @return the list of patients (could be empty)
	 * @throws OHServiceException 
	 */
	public ArrayList<Patient> getPatient() throws OHServiceException {
        return ioOperations.getPatients();
	}

	/**
	 * method that returns the full list of Patients not logically deleted by pages
	 * 
	 * @return the list of patients (could be empty)
	 * @throws OHServiceException 
	 */
	public ArrayList<Patient> getPatient(int page, int size) throws OHServiceException {
        return ioOperations.getPatients(new PageRequest(page, size));
	}

	/**
	 * method that get a Patient by his/her name
	 * 
	 * @param name
	 * @return the Patient that match specified name (could be null)
	 * @throws OHServiceException 
	 * @deprecated use getPatient(Integer code) for one patient or 
	 * getPatientWithHeightAndWeight(regex) for a list
	 */
	public Patient getPatientByName(String name) throws OHServiceException {
        return ioOperations.getPatient(name);
	}

	/**
	 * method that get a Patient by his/her ID
	 * 
	 * @param code
	 * @return the Patient (could be null)
	 * @throws OHServiceException 
	 */
	public Patient getPatientById(Integer code) throws OHServiceException {
        return ioOperations.getPatient(code);
	}
	

	/**
	 * get a Patient by his/her ID, even if he/her has been logically deleted
	 * 
	 * @param code
	 * @return the list of Patients (could be null)
	 * @throws OHServiceException 
	 */
	public Patient getPatientAll(Integer code) throws OHServiceException {
        return ioOperations.getPatientAll(code);
	}
	
	/**
	 * method that get next PAT_ID is going to be used.
	 * 
	 * @return code
	 * @throws OHServiceException 
	 */
	public int getNextPatientCode() throws OHServiceException {
        return ioOperations.getNextPatientCode();
	}

	private void buildMaritalHashMap() {
		maritalHashMap = new LinkedHashMap<String, String>();
		maritalHashMap.put("unknown", MessageBundle.getMessage("angal.patient.maritalstatusunknown"));
		maritalHashMap.put("single", MessageBundle.getMessage("angal.patient.maritalstatussingle"));
		maritalHashMap.put("married", MessageBundle.getMessage("angal.patient.maritalstatusmarried"));
		maritalHashMap.put("divorced", MessageBundle.getMessage("angal.patient.maritalstatusdivorced"));
		maritalHashMap.put("widowed", MessageBundle.getMessage("angal.patient.maritalstatuswidowed"));
	}

	public String[] getMaritalList() {	
		if (maritalHashMap == null) buildMaritalHashMap();
		String[] maritalDescriptionList = maritalHashMap.values().toArray(new String[0]);
		return maritalDescriptionList;
	}

	public String getMaritalTranslated(String maritalKey) {
		if (maritalHashMap == null) buildMaritalHashMap();
		if (maritalKey == null || !maritalHashMap.containsKey(maritalKey)) 
			return MessageBundle.getMessage("angal.patient.maritalstatusunknown"); 
		else return maritalHashMap.get(maritalKey);
	}

	public String getMaritalKey(String description) {
		if (maritalHashMap == null) buildMaritalHashMap();
		String key = "undefined";
		for (String value : maritalHashMap.keySet()) {
			if (maritalHashMap.get(value).equals(description)) {
				key = value;
				break;
			}
		}
		return key;
	}

	private void buildProfessionHashMap() {
		professionHashMap = new LinkedHashMap<String, String>();
		professionHashMap.put("unknown", MessageBundle.getMessage("angal.patient.profession.unknown"));
		professionHashMap.put("other", MessageBundle.getMessage("angal.patient.profession.other"));
		professionHashMap.put("farming", MessageBundle.getMessage("angal.patient.profession.farming"));
		professionHashMap.put("construction", MessageBundle.getMessage("angal.patient.profession.construction"));
		professionHashMap.put("medicine", MessageBundle.getMessage("angal.patient.profession.medicine"));
		professionHashMap.put("foodhospitality", MessageBundle.getMessage("angal.patient.profession.foodhospitality"));
		professionHashMap.put("homemaker", MessageBundle.getMessage("angal.patient.profession.homemaker"));
		professionHashMap.put("mechanic", MessageBundle.getMessage("angal.patient.profession.mechanic"));
		professionHashMap.put("business", MessageBundle.getMessage("angal.patient.profession.business"));
		professionHashMap.put("janitorial", MessageBundle.getMessage("angal.patient.profession.janitorial"));
		professionHashMap.put("mining", MessageBundle.getMessage("angal.patient.profession.mining"));
		professionHashMap.put("engineering", MessageBundle.getMessage("angal.patient.profession.engineering"));
	}

	public String[] getProfessionList() {	
		if (professionHashMap == null) buildProfessionHashMap();
		String[] professionDescriptionList = professionHashMap.values().toArray(new String[0]);
		return professionDescriptionList;
	}

	public String getProfessionTranslated(String professionKey) {
		if (professionHashMap == null) buildProfessionHashMap();
		if (professionKey == null || !professionHashMap.containsKey(professionKey)) 
			return MessageBundle.getMessage("angal.patient.maritalstatusunknown"); 
		else return professionHashMap.get(professionKey);
	}

	public String getProfessionKey(String description) {
		if (professionHashMap == null) buildProfessionHashMap();
		String key = "undefined";
		for (String value : professionHashMap.keySet()) {
			if (professionHashMap.get(value).equals(description)) {
				key = value;
				break;
			}
		}
		return key;
	}
	
    protected List<OHExceptionMessage> validateMergePatients(Patient mergedPatient, Patient patient2) throws OHServiceException {
        List<OHExceptionMessage> errors = new ArrayList<OHExceptionMessage>();
        boolean admitted = false;
        
        if (admissionManager.getCurrentAdmission(mergedPatient) != null) admitted = true;
        else if (admissionManager.getCurrentAdmission(patient2) != null) admitted = true;
        if (admitted) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.admission.merge"),
                    MessageBundle.getMessage("angal.admission.cannotmergeadmittedpatients"), OHSeverityLevel.ERROR));
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.admission.merge"),
                    MessageBundle.getMessage("angal.admission.patientscannothavependingtask"), OHSeverityLevel.INFO));
        }

        boolean billPending = false;
        
        ArrayList<Bill> bills = billManager.getPendingBills(mergedPatient.getCode());
        bills = billManager.getPendingBills(mergedPatient.getCode());
        if (bills != null && !bills.isEmpty()) billPending = true;
        else {
            bills = billManager.getPendingBills(patient2.getCode());
            if (bills != null && !bills.isEmpty()) billPending = true;
        }
        if (billPending) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.admission.merge"),
                    MessageBundle.getMessage("angal.admission.cannotmergewithpendingbills"), OHSeverityLevel.ERROR));
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.admission.merge"),
                    MessageBundle.getMessage("angal.admission.patientscannothavependingtask"), OHSeverityLevel.INFO));
        }
        if (mergedPatient.getSex() != patient2.getSex()) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.admission.merge"), MessageBundle.getMessage("angal.admission.selectedpatientshavedifferentsex"),
                    OHSeverityLevel.ERROR));
        }
        return errors;
    }

    /**
	 * method that logically delete a Patient (not physically deleted)
	 * 
	 * @param patient - the {@link Patient} to be deleted
	 * @return true - if the Patient has been deleted (logically)
	 * @throws OHServiceException 
	 */
	public boolean deletePatient(Patient patient) throws OHServiceException {
        return ioOperations.deletePatient(patient);
	}
	
	/**
	 * method that check if the patient's name is already present in the DB
	 * (the passed string 'name' should be a concatenation of firstName + " " + secondName
	 * 
	 * @param name - name of the patient
	 * @return true - if the patient is already present
	 * @throws OHServiceException
	 */
	public boolean isNamePresent(String name) throws OHServiceException {
        return ioOperations.isPatientPresentByName(name);
	}
	
	/**
	 * method that returns the full list of Patients not logically deleted with Height and Weight 
	 * 
	 * @param regex
	 * @return the full list of Patients with Height and Weight (could be empty)
	 * @throws OHServiceException 
	 */
	public ArrayList<Patient> getPatientWithHeightAndWeight(String regex) throws OHServiceException{
        return ioOperations.getPatientsByOneOfFieldsLike(regex);
	}

	/**
	 * method that merge patients and all clinic details under the same PAT_ID
	 * 
	 * @param mergedPatient
	 * @param patient2
	 * @return true - if no OHServiceException occurred
	 * @throws OHServiceException 
	 */
	public boolean mergePatient(Patient mergedPatient, Patient patient2) throws OHServiceException  {
			if (mergedPatient.getBirthDate() != null &&
					mergedPatient.getAgetype().compareTo("") == 0) {
				//mergedPatient only Age
				Date bdate2 = patient2.getBirthDate();
				int age2 = patient2.getAge();
				String ageType2 = patient2.getAgetype();
				if (bdate2 != null) {
					//patient2 has BirthDate
					mergedPatient.setAge(age2);
					mergedPatient.setBirthDate(bdate2);
				}
				if (bdate2 != null && ageType2.compareTo("") != 0) {
					//patient2 has AgeType 
					mergedPatient.setAge(age2);
					mergedPatient.setAgetype(ageType2);
				}
			}
			
			if (mergedPatient.getAddress().compareTo("") == 0)
				mergedPatient.setAddress(patient2.getAddress());
			
			if (mergedPatient.getCity().compareTo("") == 0)
				mergedPatient.setCity(patient2.getCity());
			
			if (mergedPatient.getNextKin().compareTo("") == 0)
				mergedPatient.setNextKin(patient2.getNextKin());
			
			if (mergedPatient.getTelephone().compareTo("") == 0)
				mergedPatient.setTelephone(patient2.getTelephone());
			
			if (mergedPatient.getMother_name().compareTo("") == 0)
				mergedPatient.setMother_name(patient2.getMother_name());
			
			if (mergedPatient.getMother() == 'U')
				mergedPatient.setMother(patient2.getMother());
			
			if (mergedPatient.getFather_name().compareTo("") == 0)
				mergedPatient.setFather_name(patient2.getFather_name());
			
			if (mergedPatient.getFather() == 'U')
				mergedPatient.setFather(patient2.getFather());
			
			if (mergedPatient.getBloodType().compareTo("") == 0)
				mergedPatient.setBloodType(patient2.getBloodType());
			
			if (mergedPatient.getHasInsurance() == 'U')
				mergedPatient.setHasInsurance(patient2.getHasInsurance());
			
			if (mergedPatient.getParentTogether() == 'U')
				mergedPatient.setParentTogether(patient2.getParentTogether());
			
			if (mergedPatient.getNote().compareTo("") == 0)
				mergedPatient.setNote(patient2.getNote());
			else {
				String note = mergedPatient.getNote();
				mergedPatient.setNote(patient2.getNote()+"\n\n"+note);
			}

            List<OHExceptionMessage> errors = validateMergePatients(mergedPatient, patient2);
            if(!errors.isEmpty()){
                throw new OHDataValidationException(errors);
            }
            return ioOperations.mergePatientHistory(mergedPatient, patient2);
	}
	
	/**
	 * Verify if the object is valid for CRUD and return a list of errors, if any
	 * @param patient
	 * @throws OHDataValidationException 
	 */
    protected void validate(Patient patient) throws OHDataValidationException{
        List<OHExceptionMessage> errors = new ArrayList<OHExceptionMessage>();

        if (StringUtils.isEmpty(patient.getFirstName())) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"), MessageBundle.getMessage("angal.patient.insertfirstname"),
                    OHSeverityLevel.ERROR));
        }
        if (StringUtils.isEmpty(patient.getSecondName())) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"), MessageBundle.getMessage("angal.patient.insertsecondname"),
                    OHSeverityLevel.ERROR));
        }
        if (!checkAge(patient)) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"), MessageBundle.getMessage("angal.patient.insertvalidage"),
                    OHSeverityLevel.ERROR));
        }
        if (StringUtils.isEmpty(String.valueOf(patient.getSex()))) {
            errors.add(new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"), "Please select a sex",
                    OHSeverityLevel.ERROR));
        }
	    if(!errors.isEmpty()){
	        throw new OHDataValidationException(errors);
	    }
    }

    private boolean checkAge(Patient patient) {
	    Date now = new Date();
        Date birthDate = patient.getBirthDate();

        if(birthDate == null || birthDate.after(now)){
            return false;
        }
        if(patient.getAge() < 0 || patient.getAge() > 200){
            return false;
        }
        return true;
    }
    
}
