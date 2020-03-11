package com.axelor.admission.service;

import com.axelor.admission.db.AdmissionEntry;
import com.axelor.admission.db.AdmissionProcess;
import com.axelor.admission.db.CollegeEntry;
import com.axelor.admission.db.Faculty;
import com.axelor.admission.db.FacultyEntry;
import com.axelor.admission.db.repo.AdmissionEntryRepository;
import com.axelor.admission.db.repo.FacultyEntryRepository;
import com.axelor.admission.db.repo.FacultyRepository;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdmissionProcessServiceImpl implements AdmissionProcessService {
	@Transactional
	@Override
	public void completeAdmission(AdmissionProcess admissionProcess) {
		
		LocalDate fromDate = admissionProcess.getFromDate();
		LocalDate toDate = admissionProcess.getToDate();

		List<Faculty> facultyList = Beans.get(FacultyRepository.class).all().fetch();

		if (facultyList != null && !facultyList.isEmpty()) {
			for (Faculty faculty : facultyList) {
				List<AdmissionEntry> admissionEntryList = Beans.get(AdmissionEntryRepository.class).all().filter(
						"self.statusSelect=2 AND self.faculty=? AND self.registrationDate>=? AND self.registrationDate <= ?",
						faculty.getId(), fromDate, toDate).fetch();
				
			//	List<AdmissionEntry> admissionEntries=admissionEntryList.stream().sorted(Comparator.comparing(AdmissionEntry::getMerit).reversed()).collect(Collectors.toList());
				Comparator<AdmissionEntry> comparator = new Comparator<AdmissionEntry>() {
					@Override
					public int compare(AdmissionEntry Ad1, AdmissionEntry Ad2) {
						int result = Ad1.getMerit().compareTo(Ad2.getMerit());
						if (result == 0) {
							return Ad1.getRegistrationDate().compareTo(Ad2.getRegistrationDate());
						}
						return result > 0 ? -1 : 1;
					}
				};
				Collections.sort(admissionEntryList, comparator);
				
				for (AdmissionEntry admissionEntry : admissionEntryList) {

					List<CollegeEntry> collegeEntries = admissionEntry.getCollegesList();
					collegeEntries.sort(Comparator.comparing(CollegeEntry::getSequence).reversed());
				
					for (CollegeEntry collegeEntry : collegeEntries) {
						FacultyEntry facultyEntry = Beans.get(FacultyEntryRepository.class).all()
								.filter("self.faculty=? AND self.college=?", faculty.getId(), collegeEntry.getCollege())
								.fetchOne();
						if (facultyEntry != null) {
							int avilableSeat = facultyEntry.getSeats();

							if (avilableSeat > 0) {
								admissionEntry.setCollegesSelected(collegeEntry.getCollege());
								admissionEntry.setValidationDate(LocalDate.now());
								admissionEntry.setStatusSelect(AdmissionEntryRepository.STATUS_ADMITTED);
								avilableSeat--;
								facultyEntry.setSeats(avilableSeat);
							}
						} else {
							admissionEntry.setStatusSelect(AdmissionEntryRepository.STATUS_CANCELED);
							admissionEntry.setValidationDate(null);
							admissionEntry.setCollegesSelected(null);
						}
					}
					if (admissionEntry.getStatusSelect() == AdmissionEntryRepository.STATUS_CONFIRM) {
						admissionEntry.setStatusSelect(AdmissionEntryRepository.STATUS_CANCELED);
						admissionEntry.setValidationDate(null);
						admissionEntry.setCollegesSelected(null);
					}
				}
			}
		}
	}
}
