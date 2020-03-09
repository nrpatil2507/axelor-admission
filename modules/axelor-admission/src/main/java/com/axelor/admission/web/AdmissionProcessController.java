package com.axelor.admission.web;

import com.axelor.admission.db.AdmissionProcess;
import com.axelor.admission.service.AdmissionProcessService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AdmissionProcessController {
  @Inject AdmissionProcessService admissionProcessService;

  public void completeAdmission(ActionRequest request, ActionResponse response) {
    AdmissionProcess admissionProcess = request.getContext().asType(AdmissionProcess.class);
    try {
      admissionProcessService.completeAdmission(admissionProcess);
      response.setFlash("Admission Process Completed");
    } catch (Exception e) {
      System.out.println("error");
    }
  }
}
