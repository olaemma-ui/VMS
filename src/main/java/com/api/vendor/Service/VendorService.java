package com.api.vendor.Service;

import com.api.vendor.Message.Response;
import com.api.vendor.Model.*;
import com.api.vendor.Model.Temp.TempDocs;
import com.api.vendor.Model.Temp.TempOwnedEquip;
import com.api.vendor.Model.Temp.TempVendor;
import com.api.vendor.Model.Temp.TempVerifiedClient;
import com.api.vendor.Repository.*;
import com.api.vendor.Util.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class VendorService {


    private Boolean success;
    private String responseCode;
    private String responseDescription;
    private Object data;
    private Object error;

    @Value("${SunTrust.ui.url}")
    private String frontendURL;

    @Value("#{'${vendor.workingCapital}'.split(',')}")
    private List<String> workingCapitals;


    @Value("#{'${vendor.businessType}'.split(',')}")
    private List<String> businessTypes;


    private final Utils utils;
    private final ObjectMapper mapper;
    private final VendorRepo vendorRepo;
    private final TempVendorRepo tempVendorRepo;
    private final TempDocsRepo tempDocsRepo;
    private final VendorDocsRepo vendorDocsRepo;
    private final ActivityRepo activityRepo;

    private final DocumentTypeRepo documentTypeRepo;

    private final List<String> documents = Arrays.asList("companyProf", "cac", "companyCert", "amlcftcpQuestionaire", "assessmentQuestionaire", "vat", "tcc");

    @Autowired
    VendorService(Utils utils, ObjectMapper mapper, VendorRepo vendorRepo, TempVendorRepo tempVendorRepo, TempDocsRepo tempDocsRepo, ActivityRepo activityRepo, VendorDocsRepo vendorDocsRepo, DocumentTypeRepo documentTypeRepo){
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.utils = utils;
        this.vendorRepo = vendorRepo;
        this.tempVendorRepo = tempVendorRepo;
        this.tempDocsRepo = tempDocsRepo;
        this.activityRepo = activityRepo;
        this.vendorDocsRepo = vendorDocsRepo;
        this.documentTypeRepo = documentTypeRepo;
    }


    public ResponseEntity<Response> getAllDocuments(){
        reset();
        try {
            success(documentTypeRepo.findAll());
        }catch (Exception e){
            responseDescription = "Something went wrong";
            e.printStackTrace();
        }
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }

    public ResponseEntity<Response> addVendor(Map<String, String> vendor){
        reset();
        String requestId = UUID.randomUUID().toString();
        if (vendor.containsKey("email") && vendor.containsKey("vendorName") && vendor.containsKey("staffId")&& vendor.containsKey("remark") && vendor.containsKey("staffName")){
            try{
                String staffRole = utils.role(vendor.get("staffId")).get(0).get("roleName").asText();
//                String staffRole = "INITIATOR";
                if (staffRole != null){
                    if (staffRole.equalsIgnoreCase("INITIATOR")){

                        if(!tempVendorRepo.findByEmail(vendor.get("email")).isPresent() && !vendorRepo.findByEmail(vendor.get("email"), "0").isPresent()){
                            String vendorId = utils.genId(tempVendorRepo.findAllId(), new int[]{10, 15});
                            TempVendor tempVendor = new TempVendor();
                            tempVendor.setOrgName(vendor.get("vendorName"));
                            tempVendor.setOrgEmail(vendor.get("email"));
                            tempVendor.setAction("NEW");
                            tempVendor.setApprovalStatus(null);
                            tempVendor.setStatus(null);
                            tempVendor.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                            tempVendor.setVendorId(vendorId);
                            tempVendor.setInitiatorId(vendor.get("staffId"));
                            tempVendor.setInitiatorName(vendor.get("staffName"));
                            tempVendor.setRequestId(requestId);
                            tempVendor.setRemark(vendor.get("remark"));

                            boolean sendMail = utils.sendMail( tempVendor.getOrgEmail(), frontendURL+"?id="+vendorId);
//                            boolean sendMail = true;
                            if(sendMail){
                                tempVendorRepo.save(tempVendor);
                                utils.saveAction(
                                        "NEW",
                                        vendor.get("staffId"),
                                        null,
                                        vendorId,
                                        vendor.get("remark"),
                                        null,
                                        requestId,
                                        "PENDING",
                                        vendor.get("staffName"),
                                        null,
                                        tempVendor.getOrgName(),
                                        tempVendor.getOrgEmail()
                                    );
                                success(tempVendor);
                            }else responseDescription = "Sorry! Error occurred sending email.";

                        }else responseDescription = "Vendor email already exist.";

                    }else responseDescription = "You're not an initiator";
                }else responseDescription = "You're not profiled";

            }catch (Exception ex){
                ex.printStackTrace();
                responseDescription = "Something went wrong!";
            }
        }
        else responseDescription = "All fields / parameters are required";

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    /**
     * Saves Vendor details for reference purpose
     * */
    public ResponseEntity<Response> vendorSave(TempVendor tempVendor){
        reset();
       try{
           responseDescription = "VendorId required";
           Optional.ofNullable(tempVendor.getVendorId()).ifPresent(
                   vendorId -> {
                       responseDescription = "Invalid vendorId.";
                       tempVendorRepo.findById(tempVendor.getVendorId()).ifPresent(
                               vendor->{
                                   System.out.println("007");
                                   if (vendor.getApprovalStatus() == null){
                                       System.out.println("006");
                                       if (tempVendor.getOrgEmail().equalsIgnoreCase(vendor.getOrgEmail())) {
                                           tempVendor.setInitiatorId(vendor.getInitiatorId());
                                           tempVendor.setAction("NEW");
                                           tempVendor.setStatus("PENDING");
                                           tempVendor.setRequestId(vendor.getRequestId());
                                           tempVendor.setApprovalStatus(null);
                                           tempVendor.setApproverId(null);
                                           tempVendor.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                                           tempVendor.setRemark(vendor.getRemark());
                                           System.out.println("005");
                                           if(tempVendor.getDocuments() != null) {
                                               System.out.println("004");
                                               List<TempDocs> tempDocs = new ArrayList<>();
                                               System.out.println("003");
                                               tempVendor.getDocuments().forEach(
                                                       e -> {
                                                           System.out.println("002");
                                                           if (documents.contains(e.getFileName())) {

                                                               AtomicReference<String> id = new AtomicReference<>(null);
                                                               vendor.getDocuments()
                                                                       .stream()
                                                                       .filter(elem -> {
                                                                           if (documents.contains(elem.getFileName())){
                                                                               id.set(elem.getDocumentId());
                                                                               return true;
                                                                           }
                                                                           return false;
                                                                       });
                                                               e.setDocumentId(Optional.ofNullable(id.get()).orElse(UUID.randomUUID().toString()));
                                                               e.setUploadedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                               e.setTempVendor(tempVendor);
                                                               tempDocs.add(e);
                                                           } else responseDescription = "Expected documents [" +
                                                                   "cac, tcc, vat, companyCert, amlcftcpQuestionaire, assessmentQuestionaire, companyProf" +
                                                                   "]";
                                                       }
                                               );
                                               tempVendor.setDocuments(tempDocs);
                                           }
                                           if (tempVendor.getOwnedEquips() != null){
                                               List<TempOwnedEquip> tempOwnedEquipList = new ArrayList<>();
                                               tempVendor.getOwnedEquips().forEach(
                                                       equip ->{
                                                           equip.setId(Optional.ofNullable(equip.getId()).orElse(UUID.randomUUID().toString()));
                                                           equip.setTempVendor(tempVendor);
                                                           tempOwnedEquipList.add(equip);
                                                       }
                                               );

                                               tempVendor.setOwnedEquips(tempOwnedEquipList);
                                           }

                                           if (tempVendor.getVerifiedClients() != null){
                                               List<TempVerifiedClient> tempVerifiedClientList = new ArrayList<>();
                                               tempVendor.getVerifiedClients().forEach(
                                                       clientele -> {
                                                           clientele.setId(Optional.ofNullable(clientele.getId()).orElse(UUID.randomUUID().toString()));
                                                           clientele.setTempVendor(tempVendor);
                                                           tempVerifiedClientList.add(clientele);
                                                       }
                                               );

                                               tempVendor.setVerifiedClients(tempVerifiedClientList);;
                                           }

                                           tempVendorRepo.save(tempVendor);
                                           success(tempVendor);
                                       }else responseDescription = "E-mail mismatch with pre-registered email!";
                                   }else responseDescription = "Already Registered!";
                               }
                       );

                   }
           );

       }catch (Exception e){
           responseDescription = "Something went wrong.";
           e.printStackTrace();
       }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    /**
     * Register vendor.
     * This submit the vendor details provided and awaits approval
     * */
    public ResponseEntity<Response> vendorRegister(TempVendor tempVendor){
        reset();
        try{
            Object[] validate = utils.validate(tempVendor, new String[]{
                    "createdAt", "approvalStatus", "status",
                    "action", "updatedAt", "status",
                    "remark", "requestId", "initiatorId", "initiatorName",
                    "approverId", "approvalStatus", "approverName", "remark"});
            data = tempVendor;
            error = validate[1];

            if (Boolean.parseBoolean(validate[0].toString())) {
                responseDescription = "vendorId is required";
                Optional.ofNullable(tempVendor.getVendorId()).ifPresent(
                        vendorId -> {
                            responseDescription = "Invalid vendorId.";
                            tempVendorRepo.findById(tempVendor.getVendorId()).ifPresent(
                                    vendor -> {
                                        if (tempVendor.getApprovalStatus() == null){
                                            if (tempVendor.getOrgEmail().equalsIgnoreCase(vendor.getOrgEmail())) {
                                                tempVendor.setOrgName(vendor.getOrgName());
                                                tempVendor.setAction("NEW");
                                                tempVendor.setStatus("PENDING");
                                                tempVendor.setApprovalStatus("PENDING");
                                                tempVendor.setInitiatorId(vendor.getInitiatorId());
                                                tempVendor.setInitiatorName(vendor.getInitiatorName());
                                                tempVendor.setApproverId(null);
                                                tempVendor.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                tempVendor.setRequestId(vendor.getRequestId());
                                                tempVendor.setRemark(vendor.getRemark());

                                                List<TempDocs> tempDocs = new ArrayList<>();
                                                tempVendor.getDocuments().forEach(
                                                        e ->{
                                                            if (documents.contains(e.getFileName())) {
                                                                AtomicReference<String> id = new AtomicReference<>(null);
                                                                vendor.getDocuments()
                                                                .stream()
                                                                .filter(elem -> {
                                                                    if (documents.contains(elem.getFileName())){
                                                                        id.set(elem.getDocumentId());
                                                                        return true;
                                                                    }
                                                                    return false;
                                                                });
                                                                e.setDocumentId(Optional.ofNullable(id.get()).orElse(UUID.randomUUID().toString()));
                                                                e.setUploadedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                                e.setTempVendor(tempVendor);
                                                                tempDocs.add(e);
                                                            }else responseDescription = "Expected documents [" +
                                                                    "cac, tcc, vat, companyCert, amlcftcpQuestionaire, assessmentQuestionaire, companyProf" +
                                                                    "]";
                                                        }
                                                );
                                                tempVendor.setDocuments(tempDocs);

                                                List<TempOwnedEquip> tempOwnedEquipList = new ArrayList<>();
                                                tempVendor.getOwnedEquips().forEach(
                                                        equip ->{
                                                            equip.setTempVendor(tempVendor);
                                                            tempOwnedEquipList.add(equip);
                                                        }
                                                );

                                                tempVendor.setOwnedEquips(tempOwnedEquipList);

                                                List<TempVerifiedClient> tempVerifiedClientList = new ArrayList<>();
                                                tempVendor.getVerifiedClients().forEach(
                                                        clientele -> {
                                                            clientele.setTempVendor(tempVendor);
                                                            tempVerifiedClientList.add(clientele);
                                                        }
                                                );

                                                tempVendor.setVerifiedClients(tempVerifiedClientList);;


                                                tempVendorRepo.save(tempVendor);
                                                success(tempVendor);
                                            }
                                            else responseDescription = "E-mail mismatch with pre-registered email!";
                                        }else responseDescription = "Already Registered!";
                                    }
                            );

                        }
                );
            }
            else responseDescription = "Invalid or required fields";
        }catch (Exception e){
            responseDescription = "Something went wrong.";
            e.printStackTrace();
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> approveVendor(JsonNode node){
        reset();
        data = node;
        if (node.has("staffId") && node.has("staffName") && node.has("vendorId") && node.has("action") && node.has("remark")){
            try{
                String staffRole = utils.role(node.get("staffId").asText()).get(0).get("roleName").asText();
//                String staffRole = "APPROVER";
                if (staffRole != null){

                    if (staffRole.equalsIgnoreCase("APPROVER") || staffRole.equalsIgnoreCase("APPROVAL")){

                        responseDescription = "Vendor does not exist or No pending approval!";
                        tempVendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                                tempVendor->{
                                    try{

                                        if (node.get("action").asText().equalsIgnoreCase("APPROVE") || node.get("action").asText().equalsIgnoreCase("DECLINE")){
                                            if (tempVendor.getApprovalStatus() != null){
                                                if (tempVendor.getApprovalStatus().equalsIgnoreCase("PENDING")) {

                                                    Optional<Vendor> optionalTempVendor = vendorRepo.findById(tempVendor.getVendorId());

                                                    tempVendor.setApproverId(node.get("staffId").asText());
                                                    tempVendor.setApproverName(node.get("staffName").asText());

                                                    Vendor vendor = mapper.convertValue(tempVendor, Vendor.class);
                                                    List<VendorDocuments> vendorDocuments = new ArrayList<>();
                                                    List<OwnedEquip> ownedEquips = new ArrayList<>();
                                                    List<VerifiedClient> verifiedClients = new ArrayList<>();

                                                    if (node.get("action").asText().equalsIgnoreCase("APPROVE")) {
                                                        if (tempVendor.getAction().equalsIgnoreCase("UPDATE DOCUMENT")){
                                                            //Get previously uploaded vendor document
                                                            List<VendorDocuments> vendorDocumentsList = vendorRepo.findByVendorId(tempVendor.getVendorId());

                                                            //Update the previously uploaded documents with the newly / updated document
                                                            for (TempDocs tempDocs : tempVendor.getDocuments()) {
                                                                for (VendorDocuments vendorDocument : vendorDocumentsList) {
                                                                    if (tempDocs.getDocumentId().equalsIgnoreCase(vendorDocument.getDocumentId())) {
                                                                        vendorDocument.setBase64(tempDocs.getBase64());
                                                                        vendorDocument.setFileName(tempDocs.getFileName());
                                                                        vendorDocument.setDocumentName(tempDocs.getDocumentName());
                                                                        vendorDocument.setUploadedAt(tempDocs.getUploadedAt());
                                                                        vendorDocument.setVendor(vendor);
                                                                    }

                                                                    vendorDocuments.add(vendorDocument);
                                                                }
                                                            }
                                                        }
                                                        else{
                                                            for (TempDocs tempDocs : tempVendor.getDocuments()) {
                                                                VendorDocuments documents = mapper.convertValue(tempDocs, VendorDocuments.class);
                                                                documents.setVendor(vendor);
                                                                vendorDocuments.add(documents);
                                                            }
                                                        }

                                                        Vendor finalVendor = vendor;
                                                        tempVendor.getOwnedEquips().forEach(
                                                                equips -> {
                                                                    OwnedEquip ownedEquip = mapper.convertValue(equips, OwnedEquip.class);
                                                                    ownedEquip.setVendor(finalVendor);
                                                                    ownedEquips.add(ownedEquip);
                                                                }
                                                        );

                                                        tempVendor.getVerifiedClients().forEach(
                                                                clientele -> {
                                                                    VerifiedClient client = mapper.convertValue(clientele, VerifiedClient.class);
                                                                    client.setVendor(finalVendor);
                                                                    verifiedClients.add(client);
                                                                }
                                                        );

                                                        vendor.setVerifiedClients(verifiedClients);
                                                        vendor.setOwnedEquips(ownedEquips);
                                                        vendor.setDocuments(vendorDocuments);

                                                        vendor.setStatus(
                                                                (tempVendor.getAction().equalsIgnoreCase("NEW") ||
                                                                (tempVendor.getAction().equalsIgnoreCase("WHITELIST") ||
                                                                (tempVendor.getAction().equalsIgnoreCase("UPDATE DOCUMENT")) ||
                                                                (tempVendor.getAction().equalsIgnoreCase("UPDATE")))
                                                                        ? "ACTIVE" : tempVendor.getAction())
                                                        );
                                                        vendor.setApprovalStatus("APPROVED");
                                                    }
                                                    else {
                                                        if (tempVendor.getAction().equalsIgnoreCase("NEW")) {
                                                            vendor.setStatus("DECLINED");
                                                        } else if (tempVendor.getAction().equalsIgnoreCase("UPDATE")) {
                                                            vendor = optionalTempVendor.get();
                                                        }
                                                        else {
                                                            vendor.setStatus(optionalTempVendor.get().getStatus());
                                                            vendor.setApprovalStatus(optionalTempVendor.get().getApprovalStatus());
                                                        }
                                                    }

                                                    vendor.setAction(null);
                                                    vendorRepo.save(vendor);
                                                    tempVendorRepo.delete(tempVendor);

                                                    utils.saveAction(
                                                            null,
                                                            tempVendor.getInitiatorId(),
                                                            node.get("staffId").asText(),
                                                            tempVendor.getVendorId(),
                                                            tempVendor.getRemark(),
                                                            node.get("remark").asText(),
                                                            tempVendor.getRequestId(),
                                                            (node.get("action").asText().equalsIgnoreCase("APPROVE")
                                                                    ? "APPROVED" : "DECLINED"),
                                                            tempVendor.getInitiatorName(),
                                                            tempVendor.getApproverName(),
                                                            tempVendor.getOrgName(),
                                                            tempVendor.getOrgEmail()
                                                    );
                                                    success(node);
                                                }
                                                else responseDescription = "No pending request or action";
                                            } else responseDescription = "No pending request or action";
                                        }
                                        else responseDescription = "Invalid action parameter";
                                    }catch (Exception ex){
                                        ex.printStackTrace();
                                        responseDescription = "Something went wrong";
                                    }
                                }
                        );

                    }else responseDescription = "Not an approver";
                }
                else responseDescription = "You're not profiled";

            }catch (Exception ex){
                ex.printStackTrace();
                responseDescription = "Something went wrong!";
            }
        }
        else responseDescription = "All fields / parameters are required [action, staffId, remark, vendorId]";

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }

//    @Transactional
    public ResponseEntity<Response> getVendorDetails(JsonNode node, String detailsType){
        reset();

        if (node.has("vendorId")){
            try{
                AtomicBoolean present = new AtomicBoolean(false);
                if (detailsType.equalsIgnoreCase("pending"))
                    tempVendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                            tempVendor -> {
                                present.set(true);
                                success(tempVendor);
                            }
                    );

                if (!present.get()){

                    success(new HashMap<String, String>(){});
                    vendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                            vendor -> {
                                present.set(true);
                                success(vendor);
                            }
                    );
                }
            }catch (Exception e){
                e.printStackTrace();
                responseDescription = "Something went wrong!";
            }

        }else responseDescription = "vendorId is required";

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> getDeclinedVendor(){
        reset();
        try{
            responseDescription = "SUCCESS";
            List<Vendor> vendors = vendorRepo.findByStatus("DECLINED");
            vendors.forEach(vendor -> {
                vendor.setDocuments(Collections.emptyList());
                vendor.setVerifiedClients(Collections.emptyList());
                vendor.setOwnedEquips(Collections.emptyList());
            });
            success(vendors);
        }catch(Exception e){
            e.printStackTrace();
            responseDescription = "Something went wrong!";
        }
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> getActiveVendors(){
        reset();

        try{
            responseDescription = "SUCCESS";
            List<Vendor> vendors = vendorRepo.findByStatus("ACTIVE");
            vendors.forEach(vendor -> {
                vendor.setDocuments(Collections.emptyList());
                vendor.setVerifiedClients(Collections.emptyList());
                vendor.setOwnedEquips(Collections.emptyList());
            });
            success(vendors);
        }catch(Exception e){
            e.printStackTrace();
            responseDescription = "Something went wrong!";
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> getPendingRequest(){
        reset();

        try{
            responseDescription = "SUCCESS";
            List<TempVendor> tempVendors = tempVendorRepo.findByStatus("PENDING");
            tempVendors.forEach(tempVendor -> {
                if (!tempVendor.getAction().equalsIgnoreCase("UPDATE DOCUMENT")) {
                    tempVendor.setDocuments(Collections.emptyList());
                    tempVendor.setVerifiedClients(Collections.emptyList());
                    tempVendor.setOwnedEquips(Collections.emptyList());
                }
            });
            success(tempVendors);
        }catch(Exception e){
            e.printStackTrace();
            responseDescription = "Something went wrong!";
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> getBlacklistVendor(){
        reset();

        try{
            responseDescription = "SUCCESS";
            List<Vendor> vendors = vendorRepo.findByStatus("BLACKLIST");
            vendors.forEach(vendor -> {
                vendor.setDocuments(Collections.emptyList());
                vendor.setVerifiedClients(Collections.emptyList());
                vendor.setOwnedEquips(Collections.emptyList());
            });
            success(vendors);
        }catch(Exception e){
            e.printStackTrace();
            responseDescription = "Something went wrong!";
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> getActivityLog(){
        reset();
        try{
            List<ActivityLog> activityLogs = activityRepo.findAll();
            success(activityLogs);
        }catch (Exception e){
            e.printStackTrace();
            responseDescription = "Something went wrong!";
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> blacklist$whitelistVendor(JsonNode node){
        reset();
        String requestId = UUID.randomUUID().toString();

        if (node.has("staffId") && node.has("vendorId") && node.has("action") && node.has("remark") && node.has("staffName")){
            if (node.get("action").asText().equalsIgnoreCase("BLACKLIST") || node.get("action").asText().equalsIgnoreCase("WHITELIST")){

                String staffRole = utils.role(node.get("staffId").asText()).get(0).get("roleName").asText();
//                String staffRole = "INITIATOR";
                if (staffRole.equalsIgnoreCase("INITIATOR")){
                   try{
                       responseDescription = "Invalid vendorId";
                       vendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                               vendor -> {

                                   Optional<TempVendor> optionalTempVendor = tempVendorRepo.findById(node.get("vendorId").asText());
                                   AtomicBoolean pending = new AtomicBoolean(false);
                                   optionalTempVendor.ifPresent(
                                           e-> pending.set(e.getApprovalStatus().equalsIgnoreCase("PENDING"))
                                   );
                                   if (!pending.get()) {

                                       TempVendor tempVendor = mapper.convertValue(vendor, TempVendor.class);
                                       tempVendor.setApprovalStatus("PENDING");
                                       tempVendor.setStatus(vendor.getStatus());
                                       tempVendor.setInitiatorId(node.get("staffId").asText());
                                       tempVendor.setAction(node.get("action").asText().toUpperCase());
                                       tempVendor.setApproverId(null);
                                       tempVendor.setRequestId(requestId);
                                       List<TempDocs> tempDocsList = new ArrayList<>();
                                       vendor.getDocuments().forEach(
                                               vendorDocuments -> {
                                                   TempDocs tempDocs = mapper.convertValue(vendorDocuments, TempDocs.class);
                                                   tempDocs.setTempVendor(tempVendor);
                                                   tempDocsList.add(tempDocs);
                                               }
                                       );
                                       tempVendor.setDocuments(tempDocsList);

                                       try {
                                           tempVendorRepo.save(tempVendor);
                                           tempVendor.setDocuments(Collections.emptyList());
                                           utils.saveAction(
                                                   node.get("action").asText().toUpperCase(),
                                                   node.get("staffId").asText(),
                                                   null,
                                                   tempVendor.getVendorId(),
                                                   node.get("remark").asText(),
                                                   null,
                                                   requestId,
                                                   "PENDING",
                                                   node.get("staffName").asText(),
                                                   null,
                                                   tempVendor.getOrgName(),
                                                   tempVendor.getOrgEmail()
                                           );
                                           success(null);
                                       } catch (Exception e) {
                                           e.printStackTrace();
                                           responseDescription = "Something went wrong!";
                                       }
                                   }
                                   else responseDescription = "Awaiting Pending approval";
                               }
                       );
                   }catch (Exception e){

                       e.printStackTrace();
                       responseDescription = "Something went wrong!";
                   }
                }else responseDescription = "Not an Initiator!";

            }else responseDescription = "All fields / parameters are required";
        }else responseDescription = "All fields are required.";

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> updateVendorDetails(TempVendor tempVendor){
        reset();
        String requestId = UUID.randomUUID().toString();
        try{
            String staffRole = utils.role(tempVendor.getInitiatorId()).get(0).get("roleName").asText();
//            String staffRole = "INITIATOR";
            if (staffRole.equalsIgnoreCase("INITIATOR")){
                Object[] validate = utils.validate(tempVendor, new String[]{"approverId", "approvalStatus", "approverName", "status", "action", "createdAt","docsList", "approverRemark", "updatedAt"});
                error = validate[1];
                data = tempVendor;
                if(Boolean.parseBoolean(validate[0].toString())){
                    Optional<TempVendor> optionalTempVendor = tempVendorRepo.findById(tempVendor.getVendorId());
                    if (!tempVendorRepo.findByEmail(tempVendor.getOrgEmail()).isPresent() && !vendorRepo.findByEmail(tempVendor.getOrgEmail(), tempVendor.getVendorId()).isPresent()){
                        AtomicBoolean pending = new AtomicBoolean(false);
                        optionalTempVendor.ifPresent(
                                e-> pending.set(e.getApprovalStatus().equalsIgnoreCase("PENDING"))
                        );

                        if (!pending.get()) {
                            responseDescription = "Invalid vendorId or not active";
                            vendorRepo.findById(tempVendor.getVendorId()).ifPresent(
                                    vendor ->
                                    {
                                        tempVendor.setApprovalStatus("PENDING");
                                        tempVendor.setAction("UPDATE");
                                        tempVendor.setStatus(null);

                                        System.out.println(tempVendor.getVerifiedClients());
                                        System.out.println(tempVendor.getOwnedEquips());

//                                        List<TempDocs> vendorDocuments = new ArrayList<>();
                                        tempVendor.getDocuments().forEach(
                                                tempDocs -> {
                                                    tempDocs.setTempVendor(tempVendor);
                                                }
                                        );

                                        List<TempOwnedEquip> tempOwnedEquipList = new ArrayList<>();
                                        tempVendor.getOwnedEquips().forEach(
                                                equip ->{
                                                    equip.setTempVendor(tempVendor);
                                                    tempOwnedEquipList.add(equip);
                                                }
                                        );

                                        tempVendor.setOwnedEquips(tempOwnedEquipList);

                                        List<TempVerifiedClient> tempVerifiedClientList = new ArrayList<>();
                                        tempVendor.getVerifiedClients().forEach(
                                                clientele -> {
                                                    clientele.setTempVendor(tempVendor);
                                                    tempVerifiedClientList.add(clientele);
                                                }
                                        );
                                        tempVendor.setVerifiedClients(tempVerifiedClientList);
                                        tempVendor.setRequestId(requestId);

                                        try {
                                            utils.saveAction(
                                                    "UPDATE",
                                                    tempVendor.getInitiatorId(),
                                                    null,
                                                    tempVendor.getVendorId(),
                                                    tempVendor.getRemark(),
                                                    null,
                                                    requestId,
                                                    "PENDING",
                                                    tempVendor.getInitiatorName(),
                                                    null,
                                                    tempVendor.getOrgName(),
                                                    tempVendor.getOrgEmail()
                                            );
                                            tempVendorRepo.save(tempVendor);
                                            tempVendor.setDocuments(Collections.emptyList());
                                            success(tempVendor);
                                        } catch (Exception e) {
                                            responseDescription = "Something ent wrong!";
                                            e.printStackTrace();
                                        }

                                    }
                            );
                        }else responseDescription = "Awaiting pending approval!";
                    }else responseDescription = "Vendor email already exist.";

                }else responseDescription = "Invalid or required fields";
            }else responseDescription = "Not an Initiator!";
        }catch (Exception e){
            responseDescription = "Something went wrong.";
            e.printStackTrace();
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> updateDocument(Map<String, Object> document){
        reset();
        List<String> documentsName = documents;
        String requestId = UUID.randomUUID().toString();
        List<Object> errorList = new ArrayList<>();
        if (document.containsKey("vendorId") && document.containsKey("documents") && document.containsKey("staffId") && document.containsKey("staffName")){

            String staffRole = utils.role(document.get("staffId").toString()).get(0).get("roleName").asText();
//            String staffRole = "INITIATOR";
           if (staffRole.equalsIgnoreCase("INITIATOR")){
               responseDescription = "Vendor does not exist Or not active!";
               vendorRepo.findById(document.get("vendorId").toString()).ifPresent(
                       vendor->{
                           if (!vendor.getStatus().equalsIgnoreCase("BLACKLIST")){

                               Optional<TempVendor> optionalTempVendor = tempVendorRepo.findById(document.get("vendorId").toString());
                               AtomicBoolean pending = new AtomicBoolean(false);
                               optionalTempVendor.ifPresent(
                                       e-> pending.set(e.getApprovalStatus().equalsIgnoreCase("PENDING"))
                               );

                               if (!pending.get()){

                                   TempVendor tempVendor = mapper.convertValue(vendor, TempVendor.class);
                                   tempVendor.setApprovalStatus("PENDING");
                                   tempVendor.setAction("UPDATE DOCUMENT");
                                   tempVendor.setStatus(null);
                                   tempVendor.setRequestId(requestId);
                                   tempVendor.setInitiatorId(document.get("staffId").toString());
                                   tempVendor.setInitiatorName(document.get("staffName").toString());
                                   List<TempDocs> tempDocsList = new ArrayList<>();
                                   List<?> tempDocs = mapper.convertValue(document.get("documents"), List.class);
                                   tempDocs.forEach(
                                           (tempDocObj)->{
                                               Object[] validate = utils.validate(tempDocObj, new String[]{"tempVendor", "uploadedAt", "documentId"});
                                               errorList.add(validate[1]);
                                               if (Boolean.parseBoolean(validate[0].toString())){
                                                   TempDocs tempDoc = mapper.convertValue(tempDocObj, TempDocs.class);
                                                   if (documentsName.contains(tempDoc.getFileName())){
                                                       tempDoc.setTempVendor(tempVendor);
                                                       tempDocsList.add(tempDoc);
                                                       documentsName.remove(tempDoc.getFileName());
                                                   }
//                                                   vendorDocsRepo.findById(tempDoc.getDocumentId()).ifPresent(
//                                                           vendorDocuments -> {
//                                                               tempDoc.setDocumentName(vendorDocuments.getDocumentName());
//                                                               tempDoc.setTempVendor(tempVendor);
//                                                               tempDocsList.add(tempDoc);
//                                                           }
//                                                   );

                                                   if (tempDocsList.size() == tempDocs.size()){
                                                       tempVendor.setDocuments(tempDocsList);
                                                       try {

                                                           utils.saveAction(
                                                               "UPDATE DOCUMENT",
                                                               tempVendor.getInitiatorId(),
                                                               null,
                                                               tempVendor.getVendorId(),
                                                               "Vendor Update Document",
                                                               null,
                                                               requestId,
                                                               "PENDING",
                                                               document.get("staffName").toString(),
                                                               null,
                                                                   tempVendor.getOrgName(),
                                                                   tempVendor.getOrgEmail()
                                                           );
                                                           tempVendorRepo.save(tempVendor);
//                                            tempVendor.setDocuments(Collections.emptyList());
                                                           success(tempVendor);
                                                       } catch (Exception e) {
                                                           responseDescription = "Something ent wrong!";
                                                           e.printStackTrace();
                                                       }
                                                   }
                                                   else responseDescription = "One of the documents field is invalid!";
                                               }
                                               else responseDescription = "All fields are required!";
                                           }
                                   );

                               }else responseDescription = "Awaiting pending approval!";

                           }
                           else responseDescription = "Vendor blacklisted!";
                       }
               );
           }else responseDescription = "Not an INITIATOR!";
        }else responseDescription = "All fields are required!";

        data = document;
        error = errorList;
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> getAllVendors(){
        reset();

        try{

            List<TempVendor> tempVendors = tempVendorRepo.findAll();
            for (TempVendor tempVendor : tempVendors) {
                tempVendor.setDocuments(Collections.emptyList());
                tempVendor.setVerifiedClients(Collections.emptyList());
                tempVendor.setOwnedEquips(Collections.emptyList());
            }


            List<Vendor> vendors = vendorRepo.findAll();
            for (Vendor vendor : vendors) {
                boolean rem =  tempVendors.removeIf(
                        e -> e.getVendorId()
                                .equalsIgnoreCase(vendor.getVendorId()));

                vendor.setDocuments(Collections.emptyList());
                vendor.setVerifiedClients(Collections.emptyList());
                vendor.setOwnedEquips(Collections.emptyList());
            }
            List<Object> allVendor = new ArrayList<>(Collections.emptyList());
            allVendor.addAll(tempVendors);
            allVendor.addAll(vendors);

            success(allVendor);
        }catch (Exception e){
            e.printStackTrace();
            responseDescription = "Something went wrong!";
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response>  businessTypeList(){
        reset();
        success(businessTypes);
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response>  workingCapital(){
        reset();
        success(workingCapitals);
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> login(JsonNode node){
        reset();

        try{

            if(node.has("username") && node.has("password")){

                HttpEntity<JsonNode> entity = new HttpEntity<>(node);
                ResponseEntity<JsonNode> response = new RestTemplate().exchange(
                        "http://10.11.200.98/stbwebservice/ValidateStaffUser",
                        HttpMethod.POST,
                        entity, JsonNode.class
                );
                if (response.getStatusCodeValue() == 200){
                    if (!response.getBody().get("responseCode").asText().equalsIgnoreCase("99")){

                        success(new HashMap<String, Object>(){{
                            put("staffId", response.getBody().get("staffId").asText());
                            put("role", utils.role(response.getBody().get("staffId").asText()));
                        }});

                    }else responseDescription = response.getBody().get("responseDescription").asText();
                }

            }else responseDescription = "All fields are required!";

        }catch(HttpClientErrorException | HttpServerErrorException e){
            e.printStackTrace();
            data = e.getResponseBodyAsString();
        }

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    private void reset(){
        success = false;
        responseCode = "96";
        responseDescription = "FAILED";
        data = null;
        error = null;
    }


    private void success(Object data){
        responseDescription = "SUCCESS";
        responseCode = "00";
        success = true;
        this.data = data;
    }
}
