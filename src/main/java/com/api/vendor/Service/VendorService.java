package com.api.vendor.Service;

import com.api.vendor.Message.Response;
import com.api.vendor.Model.*;
import com.api.vendor.Model.Temp.TempDocs;
import com.api.vendor.Model.Temp.TempVendor;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VendorService {


    private Boolean success;
    private String responseCode;
    private String responseDescription;
    private Object data;
    private Object error;

    @Value("${SunTrust.ui.url}")
    private String frontendURL;


    private final Utils utils;
    private final ObjectMapper mapper;
    private final VendorRepo vendorRepo;
    private final TempVendorRepo tempVendorRepo;
    private final TempDocsRepo tempDocsRepo;
    private final ActivityRepo activityRepo;

    @Autowired
    VendorService(Utils utils, ObjectMapper mapper, VendorRepo vendorRepo, TempVendorRepo tempVendorRepo, TempDocsRepo tempDocsRepo, ActivityRepo activityRepo){
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.utils = utils;
        this.vendorRepo = vendorRepo;
        this.tempVendorRepo = tempVendorRepo;
        this.tempDocsRepo = tempDocsRepo;
        this.activityRepo = activityRepo;
    }

    public ResponseEntity<Response> addVendor(Map<String, String> vendor){
        reset();
        String requestId = UUID.randomUUID().toString();
        if (vendor.containsKey("email") && vendor.containsKey("vendorName") && vendor.containsKey("staffId")&& vendor.containsKey("remark")){
            try{
//                String staffRole = utils.role(vendor.get("staffId")).get(0).get("roleName").asText();
                String staffRole = "INITIATOR";
                if (staffRole != null){
                    if (staffRole.equalsIgnoreCase("INITIATOR")){

                        if(!tempVendorRepo.findByEmail(vendor.get("email")).isPresent() && !vendorRepo.findByEmail(vendor.get("email")).isPresent()){
                            String vendorId = utils.genId(tempVendorRepo.findAllId(), new int[]{10, 15});
                            TempVendor tempVendor = new TempVendor();
                            tempVendor.setOrgName(vendor.get("vendorName"));
                            tempVendor.setOrgEmail(vendor.get("email"));
                            tempVendor.setAction("NEW");
                            tempVendor.setApprovalStatus(null);
                            tempVendor.setStatus(null);
                            tempVendor.setVendorId(vendor.get("staffId"));
                            tempVendor.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                            tempVendor.setVendorId(vendorId);
                            tempVendor.setInitiatorId(vendor.get("staffId"));
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
                                        null,
                                        vendor.get("remark"),
                                        null,
                                        requestId,
                                        "PENDING"
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
    public ResponseEntity<Response> vendorSave(
        String vendorDetails,
        MultipartFile companyProf,
        MultipartFile cac,
        MultipartFile companyCert,
        MultipartFile amlcftcpQuestionaire,
        MultipartFile assessmentQuestionaire,
        MultipartFile vat,
        MultipartFile tcc
    ){
        reset();
       try{
           TempVendor tempVendor = mapper.readValue(vendorDetails, TempVendor.class);
           responseDescription = "VendorId required";
           Optional.ofNullable(tempVendor.getVendorId()).ifPresent(
                   vendorId -> {
                       responseDescription = "Invalid vendorId.";
                       tempVendorRepo.findById(tempVendor.getVendorId()).ifPresent(
                               vendor->{
                                   if (vendor.getApprovalStatus() == null){
                                       if (tempVendor.getOrgEmail().equalsIgnoreCase(vendor.getOrgEmail())) {
                                           tempVendor.setInitiatorId(vendor.getInitiatorId());
                                           tempVendor.setAction("NEW");
                                           tempVendor.setStatus("PENDING");
                                           tempVendor.setRequestId(vendor.getRequestId());
                                           tempVendor.setApprovalStatus(null);
                                           tempVendor.setApproverId(null);
                                           tempVendor.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                                           tempVendor.setRemark(vendor.getRemark());

                                           Map<String, MultipartFile> files = new HashMap<>();
                                           files.put("companyCert", companyCert);
                                           files.put("cac", cac);
                                           files.put("amlcftcpQuestionaire", amlcftcpQuestionaire);
                                           files.put("assessmentQuestionaire", assessmentQuestionaire);
                                           files.put("companyProf", companyProf);
                                           files.put("cc", tcc);
                                           files.put("vat", vat);
                                           List<TempDocs> tempDocsList = new ArrayList<>();
                                           files.forEach(
                                                   (k, v) -> {
                                                       try {
                                                           TempDocs docs = new TempDocs();
                                                           docs.setDocumentId(UUID.randomUUID().toString());
                                                           docs.setFileName(v.getOriginalFilename());
                                                           docs.setUploadedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                           docs.setTempVendor(tempVendor);
                                                           docs.setBase64(Base64.getEncoder().encode(v.getBytes()));
                                                           docs.setTitle(k);
                                                           tempDocsList.add(docs);
                                                       } catch (IOException e) {
                                                           responseDescription = "Something went wrong.";
                                                           e.printStackTrace();
                                                       }
                                                   }
                                           );

                                           tempVendor.setDocsList(tempDocsList);

                                           System.out.println(tempVendor);
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
    public ResponseEntity<Response> vendorRegister(
            String vendorDetails,
            MultipartFile companyProf,
            MultipartFile cac,
            MultipartFile companyCert,
            MultipartFile amlcftcpQuestionaire,
            MultipartFile assessmentQuestionaire,
            MultipartFile vat,
            MultipartFile tcc
    ){
        reset();
        try{
            TempVendor tempVendor = mapper.readValue(vendorDetails, TempVendor.class);
            Object[] validate = utils.validate(tempVendor, new String[]{"createdAt", "approvalStatus", "status", "docsList", "action", "updatedAt", "updatedAt", "remark"});
            data = tempVendor;
            error = validate[1];
            if(companyCert != null && cac != null && amlcftcpQuestionaire != null && assessmentQuestionaire !=null && companyProf !=null){
                if (Boolean.parseBoolean(validate[0].toString())) {
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
                                                    tempVendor.setApproverId(null);
                                                    tempVendor.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                    tempVendor.setRequestId(vendor.getRequestId());
                                                    tempVendor.setRemark(vendor.getRemark());

                                                    Map<String, MultipartFile> files = new HashMap<>();
                                                    files.put("companyCert", companyCert);
                                                    files.put("cac", cac);
                                                    files.put("amlcftcpQuestionaire", amlcftcpQuestionaire);
                                                    files.put("assessmentQuestionaire", assessmentQuestionaire);
                                                    files.put("companyProf", companyProf);
                                                    files.put("vat", vat);
                                                    files.put("tcc", tcc);
                                                    List<TempDocs> tempDocsList = new ArrayList<>();
                                                    files.forEach(
                                                            (k, v) -> {
                                                                try {
                                                                    TempDocs docs = new TempDocs();
                                                                    docs.setDocumentId(UUID.randomUUID().toString());
                                                                    docs.setFileName(v.getOriginalFilename());
                                                                    docs.setUploadedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                                    docs.setTempVendor(tempVendor);
                                                                    docs.setBase64(Base64.getEncoder().encode(v.getBytes()));
                                                                    docs.setTitle(k);
                                                                    tempDocsList.add(docs);
                                                                } catch (IOException e) {
                                                                    responseDescription = "Something went wrong.";
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                    );

                                                    tempVendor.setDocsList(tempDocsList);
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
            }else responseDescription = "All necessary documents are required!";
        }catch (Exception e){
            responseDescription = "Something went wrong.";
            e.printStackTrace();
        }
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> approveVendor(JsonNode node){
        reset();

        if (node.has("staffId") && node.has("vendorId") && node.has("action") && node.has("remark")){
            try{
//                String staffRole = utils.role(node.get("staffId").asText()).get(0).get("roleName").asText();
                String staffRole = "APPROVER";
                if (staffRole != null){

                    if (staffRole.equalsIgnoreCase("APPROVER")){

                        responseDescription = "Vendor does not exist or No pending approval!";
                        tempVendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                                tempVendor->{
                                    try{

                                        if (node.get("action").asText().equalsIgnoreCase("APPROVE") || node.get("action").asText().equalsIgnoreCase("DECLINE")){
                                            if (tempVendor.getApprovalStatus() != null){
                                                if (tempVendor.getApprovalStatus().equalsIgnoreCase("PENDING")) {

                                                    Optional<Vendor> optionalTempVendor = vendorRepo.findById(tempVendor.getVendorId());
//                                                    AtomicBoolean pending = new AtomicBoolean(false);

                                                    tempVendor.setApproverId(node.get("staffId").asText());

                                                    Vendor vendor = mapper.convertValue(tempVendor, Vendor.class);
                                                    List<VendorDocuments> vendorDocuments = new ArrayList<>();

                                                    if (node.get("action").asText().equalsIgnoreCase("APPROVE")) {
                                                        if (tempVendor.getAction().equalsIgnoreCase("UPDATE DOCUMENT")){
                                                            //Get previously uploaded vendor document
                                                            List<VendorDocuments> vendorDocumentsList = vendorRepo.findByVendorId(tempVendor.getVendorId());

                                                            //Update the previously uploaded documents with the newly / updated document
                                                            for (TempDocs tempDocs : tempVendor.getDocsList()) {
                                                                for (VendorDocuments vendorDocument : vendorDocumentsList) {
                                                                    if (tempDocs.getDocumentId().equalsIgnoreCase(vendorDocument.getDocumentId())) {
                                                                        vendorDocument.setBase64(tempDocs.getBase64());
                                                                        vendorDocument.setFileName(tempDocs.getFileName());
                                                                        vendorDocument.setTitle(tempDocs.getTitle());
                                                                        vendorDocument.setUploadedAt(tempDocs.getUploadedAt());
                                                                        vendorDocument.setVendor(vendor);
                                                                    }

                                                                    vendorDocuments.add(vendorDocument);
                                                                }
                                                            }
                                                        }else{
                                                            for (TempDocs tempDocs : tempVendor.getDocsList()) {
                                                                System.out.println("docs");
                                                                VendorDocuments documents = mapper.convertValue(tempDocs, VendorDocuments.class);
                                                                documents.setVendor(vendor);
                                                                vendorDocuments.add(documents);
                                                            }
                                                        }
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
//                                                    tempVendor.setApprovalStatus(
//                                                            (node.get("action").asText().equalsIgnoreCase("APPROVE")
//                                                                    ? "APPROVED" : "DECLINED")
//                                                    );
//                                                    tempVendor.setDocsList(!(tempVendor.getAction().equalsIgnoreCase("UPDATE DOCUMENT"))
//                                                            ? Collections.emptyList() : tempVendor.getDocsList()
//                                                    );

                                                    utils.saveAction(
                                                            null,
                                                            tempVendor.getInitiatorId(),
                                                            node.get("staffId").asText(),
                                                            tempVendor.getVendorId(),
                                                            tempVendor.getRemark(),
                                                            node.get("remark").asText(),
                                                            tempVendor.getRequestId(),
                                                            (node.get("action").asText().equalsIgnoreCase("APPROVE")
                                                                    ? "APPROVED" : "DECLINED")
                                                    );
                                                    success(null);
                                                } else responseDescription = "No pending request or action";
                                            } else responseDescription = "No pending request or action";
                                        } else responseDescription = "Invalid action parameter";
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


    public ResponseEntity<Response> getVendorDetails(JsonNode node){
        reset();

        if (node.has("vendorId")){
            try{
                AtomicBoolean present = new AtomicBoolean(false);
                tempVendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                        tempVendor -> {
                            present.set(true);
                            success(tempVendor);
                        }
                );
                if (!present.get()){
                    vendorRepo.findById(node.get("vendorId").asText()).ifPresent(
                            vendor -> {
                                present.set(true);
                                success(vendor);
                            }
                    );
                }else {
                    responseDescription = "Vendor does not exist";
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
            vendors.forEach(vendor -> vendor.setDocuments(null));
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
            vendors.forEach(vendor -> vendor.setDocuments(null));
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
            tempVendors.forEach(vendor -> {
                if (!vendor.getAction().equalsIgnoreCase("UPDATE DOCUMENT"))
                    vendor.setDocsList(null);
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
            vendors.forEach(vendor -> vendor.setDocuments(null));
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

        if (node.has("staffId") && node.has("vendorId") && node.has("action") && node.has("remark")){
            if (node.get("action").asText().equalsIgnoreCase("BLACKLIST") || node.get("action").asText().equalsIgnoreCase("WHITELIST")){
//                String staffRole = utils.role(node.get("staffId").asText()).get(0).get("roleName").asText();
                String staffRole = "INITIATOR";
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
                                       tempVendor.setDocsList(tempDocsList);

                                       try {
                                           tempVendorRepo.save(tempVendor);
                                           tempVendor.setDocsList(Collections.emptyList());
                                           utils.saveAction(
                                                   node.get("action").asText().toUpperCase(),
                                                   node.get("staffId").asText(),
                                                   null,
                                                   tempVendor.getVendorId(),
                                                   node.get("remark").asText(),
                                                   null,
                                                   requestId,
                                                   "PENDING"
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

            }else responseDescription = "All fields / parameters are required [staffId, vendorId, action]";
        }else responseDescription = "All fields are required [staffId, vendorId, action].";

        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> updateVendorDetails(TempVendor tempVendor){
        reset();
        String requestId = UUID.randomUUID().toString();
        try{
//            String staffRole = utils.role(tempVendor.getInitiatorId()).get(0).get("roleName").asText();
            String staffRole = "INITIATOR";
            if (staffRole.equalsIgnoreCase("INITIATOR")){
                Object[] validate = utils.validate(tempVendor, new String[]{"approverId", "approvalStatus", "status", "action", "createdAt","docsList", "approverRemark", "updatedAt"});
                error = validate[1];
                data = tempVendor;
                if(Boolean.parseBoolean(validate[0].toString())){
                    Optional<TempVendor> optionalTempVendor = tempVendorRepo.findById(tempVendor.getVendorId());
                    AtomicBoolean pending = new AtomicBoolean(false);
                    optionalTempVendor.ifPresent(
                            e-> pending.set(e.getApprovalStatus().equalsIgnoreCase("PENDING"))
                    );

                    if (!pending.get()) {
                        responseDescription = "Invalid vendorId or not active";
                        System.out.println("id = " + tempVendor.getVendorId());
                        vendorRepo.findById(tempVendor.getVendorId()).ifPresent(
                                vendor ->
                                {
                                    tempVendor.setApprovalStatus("PENDING");
                                    tempVendor.setAction("UPDATE");
                                    tempVendor.setStatus(null);
                                    List<TempDocs> vendorDocuments = new ArrayList<>();
                                    vendor.getDocuments().forEach(
                                            tempDocs -> {
                                                TempDocs documents = new TempDocs();
                                                documents.setDocumentId(tempDocs.getDocumentId());
                                                documents.setTitle(tempDocs.getTitle());
                                                documents.setBase64(tempDocs.getBase64());
                                                documents.setFileName(tempDocs.getFileName());
                                                documents.setTempVendor(tempVendor);
                                                vendorDocuments.add(documents);
                                            }
                                    );
                                    tempVendor.setRequestId(requestId);
                                    tempVendor.setDocsList(vendorDocuments);

                                    try {
                                        tempVendorRepo.save(tempVendor);
                                        tempVendor.setDocsList(Collections.emptyList());
                                        utils.saveAction(
                                                "UPDATE",
                                                tempVendor.getInitiatorId(),
                                                null,
                                                tempVendor.getVendorId(),
                                                tempVendor.getRemark(),
                                                null,
                                                requestId,
                                                "PENDING"
                                        );
                                        tempVendorRepo.save(tempVendor);
                                        tempVendor.setDocsList(Collections.emptyList());
                                        success(tempVendor);
                                    } catch (Exception e) {
                                        responseDescription = "Something ent wrong!";
                                        e.printStackTrace();
                                    }

                                }
                        );
                    }
                }else responseDescription = "Invalid or required fields";
            }else responseDescription = "Not an Initiator!";
        }catch (Exception e){
            responseDescription = "Something went wrong.";
            e.printStackTrace();
        }
        return new ResponseEntity<>(new Response(success, responseCode, responseDescription, data, error), HttpStatus.OK);
    }


    public ResponseEntity<Response> updateDocs(MultipartFile file, String node){
        reset();
        String requestId = UUID.randomUUID().toString();

        try{
            JsonNode details = mapper.readValue (node, JsonNode.class);
            System.out.println(details);
//            String staffRole = utils.role(details.get("staffId").asText()).get(0).get("roleName").asText();
            String staffRole = "INITIATOR";
            if (staffRole.equalsIgnoreCase("INITIATOR")) {

                if (details.has("staffId") && details.has("remark") && details.has("documentId") && details.has("vendorId")) {
                    responseDescription = "Vendor does not exist or not active!";
                    vendorRepo.findById(details.get("vendorId").asText()).ifPresent(
                            vendor -> {
                                Optional<TempVendor> optionalTempVendor = tempVendorRepo.findById(details.get("vendorId").asText());
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

                                    List<TempDocs> vendorDocuments = new ArrayList<>();
                                    vendor.getDocuments().forEach(
                                            docs->{

                                                System.out.println("dociD = "+docs.getDocumentId());
                                                System.out.println("details dociD = "+details.get("documentId").asText());

                                                TempDocs documents = new TempDocs();
                                                if (details.get("documentId").asText().equalsIgnoreCase(docs.getDocumentId())){
                                                    try {
                                                        documents.setDocumentId(docs.getDocumentId());
                                                        documents.setTitle(docs.getTitle());
                                                        documents.setBase64(Base64.getEncoder().encode(file.getBytes()));
                                                        documents.setFileName(file.getOriginalFilename());
                                                        documents.setUploadedAt(Timestamp.valueOf(LocalDateTime.now()));
                                                    } catch (IOException e) {
                                                        responseDescription = "Error occurred uploading file!.";
                                                        e.printStackTrace();
                                                    }
                                                    vendorDocuments.add(documents);
                                                    documents.setTempVendor(tempVendor);
                                                }
                                            }
                                    );
                                    tempVendor.setDocsList(vendorDocuments);

                                    try {
                                        tempVendorRepo.save(tempVendor);

                                        System.out.println("docs update = "+ tempVendor.toString());
                                        utils.saveAction(
                                                "UPDATE DOCUMENT",
                                                tempVendor.getInitiatorId(),
                                                null,
                                                tempVendor.getVendorId(),
                                                details.get("remark").asText(),
                                                null,
                                                requestId,
                                                "PENDING"
                                        );
                                        success(null);
                                    } catch (Exception e) {
                                        responseDescription = "Something ent wrong!";
                                        e.printStackTrace();
                                    }
                                }

                            }
                    );
                }
                else responseDescription = "All fields are required [staffId, remark, documentId, vendorId]";
            }else responseDescription = "Not an Initiator!";
        }catch (Exception e){
            responseDescription = "Something went wrong!";
            e.printStackTrace();
        }
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
                        success(utils.role(response.getBody().get("staffId").asText()));
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
