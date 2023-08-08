package com.api.vendor.Util;


import com.api.vendor.Model.ActivityLog;
import com.api.vendor.Repository.ActivityRepo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Component
public class Utils {

    @Value("${SunTrust.mail.url}")
    private String mailUrl;

    @Value("${SunTrust.roleManagement.url}")
    private String roleMngmtUrl;

    private final ActivityRepo activityRepo;

    Utils(ActivityRepo activityRepo){
        this.activityRepo = activityRepo;
    }

    private final List<String> extensions = Arrays.asList("jpg", "jpeg", "png", "doc", "docx", "pdf", "txt", "xls", "xlsx");

    /**
     * Generate random unique randomString
     * from the pattern provided
     * */
    public String genId(List<String> prevData, int[] pattern){
        AtomicBoolean gen = new AtomicBoolean(true);
        String alpha = "";
        if (pattern.length != 2){
            return null;
        }
        while(gen.get()){
            String id = "";
            gen.set(false);
            for(int j = 0; j < pattern[0]; j++){
                int k = (new Random().nextInt(90+1 - 65 )+65);
                alpha+=(char) k;
            }
            for (int i = 0; i < pattern[1]; i++) {
                int k = (new Random().nextInt(9));
                alpha+=k;
            }
            for (String prevId: prevData) {
                if (prevId.equalsIgnoreCase(alpha)){
                    gen.set(true);
                    alpha = "";
                }
            }
        }
        return alpha.toLowerCase();
    }


    /**
     * This method validate fields. <br>
     * This method accepts the POJO class / Model that will be validated
     * @param field this is the class that needs to be validated
     * @apiNote The name should be descriptive for what it is used for and must contain the name.
     * @example Email-address, email, email_address.
     * @fields [email, password, name, mobile, country-code, text]
     * */
    public Object[] validate(Object field, String[] ignore){
        AtomicBoolean valid = new AtomicBoolean(true);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, Object> data = mapper.convertValue(field, Map.class);

        //== == == Fields to be ignored during validation
        for (String s : ignore) {
            data.remove(s);
        }

        //Regex patter for each fields.
        Map<String, String[]> regex = new HashMap<>();
        regex.put("email", new String[]{
                "^(.+)@(.+)$",
                "Invalid E-mail address"
        });

        regex.put("password", new String[]{
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
                "Must contain at least aA-zZ, 0-9 and special characters"
        });

        regex.put("name", new String[]{
                "^[A-Za-z][A-Za-z\\'\\-]+([\\ A-Za-z][A-Za-z\\'\\-]+)*",
                "Name can only be from [a-z A-Z'-]"
        });

        regex.put("mobile", new String[]{
                "^\\\\+?[1-9][0-9]{7,14}$",
                "Mobile number can only be 11 digits and b/w [0-9]"
        });

        regex.put("countryCode", new String[]{
                "^(\\+?\\d{1,3}|\\d{1,4})$",
                "Invalid country code"
        });

        regex.put("text", new String[]{
                "^[0-9A-Za-z\\s\\w]$",
                "Invalid text value"
        });
        System.out.println("data = "+data);
        data.forEach(
                (k,v)->{
                    if (data.get(k) == null){
                        valid.set(false);
                        data.put(k, "This field is required");
                    }else if (data.get(k).toString().trim().isEmpty()){
                        valid.set(false);
                        data.put(k, "This field is required");
                    }
                    else{
                        //Checks if the model contains the regex fields
                        regex.forEach(
                                (k1, v1)->{
                                    boolean text = k.contains("text")
                                            ? data.replace(k, v, htmlSpecialChars(v.toString(), false))
                                            : data.replace(k, v, htmlSpecialChars(v.toString(), true));

                                    if (k.toLowerCase().contains(k1.toLowerCase())){
                                        if (Pattern.compile(regex.get(k1)[0]).matcher(v.toString()).find()){
                                            data.replace(k, null);
                                        }else{
                                            valid.set(false);
                                            data.replace(k, regex.get(k1)[1]);
                                        }
                                    }else data.replace(k, null);
                                }
                        );
                    }
                }
        );
        return new Object[]{valid.get(), data};
    }


    /**
     * This method replace unwanted html characters to the text value
     * @param data this is data that will be stripped
     * */
    private String htmlSpecialChars(String data, boolean remove){
        data = data.replace("&", (remove) ? "" : "&amp;");
        data.replace("\"", (remove) ? "" : "&quot;");
        data.replace("<", (remove) ? "" : "&lt;");
        data.replace(">", (remove) ? "" : "&gt;");
        data.replace(">", (remove) ? "" : "&gt;");
        data.replace("=", (remove) ? "" : "&#61;");
        return data;
    }


    public boolean sendMail (String mailTo, String message, String vendorName){

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        String htmlBody =
                "<p>Dear "+vendorName+",</p>" +
                    "<p> Your profile has been created follow the link to complete registration <br>"+message+"</p>";

        String request = "{\n" +
                "        \"subject\" :\"SunTrust Vendor\",\n" +
                "        \"sender\" :\"notification@suntrustng.com\",\n" +
                "        \"recipient\":\""+mailTo+"\",\n" +
                "        \"message\":\""+htmlBody+"\",\n" +
                "        \"Bcc\":\""+mailTo+"\",\n" +
                "        \"IsHtml\":true,\n" +
                "        \"AppName\" :\"Vendor management system\"\n" +
                "}";

        System.out.println(request);

        ResponseEntity<JsonNode> node = new RestTemplate().exchange(
                mailUrl, HttpMethod.POST,
                new HttpEntity<>(request, headers), JsonNode.class
        );

        if (node.getStatusCodeValue() == 200){
            return (node.getBody().get("responseCode").asText().equalsIgnoreCase("00"));
        }
        return false;
    }


    public String fileExt(String file){
        String[] dots = file.split("\\.");
        return dots[dots.length-1];
    }


    /**
     * @param action
     * @apiNote
     * */
    public void saveAction(
            String action, String initiatorId, String approverId,
            String vendorId, String initiatorRemark, String approverRemark,
            String requestId, String status, String initiatorName, String approverName,
            String vendorName, String vendorEmail
    ) throws Exception {
        try {

            Optional<ActivityLog> log = activityRepo.findById(requestId);
            ActivityLog activityLog = new ActivityLog();
            activityLog.setId(requestId);
            activityLog.setAction(action == null ? log.get().getAction(): action);
            activityLog.setVendorName(action == null ? log.get().getVendorName(): vendorName);
            activityLog.setVendorEmail(action == null ? log.get().getVendorEmail(): vendorEmail);
            activityLog.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            activityLog.setVendorId(vendorId);
            activityLog.setVendorEmail(vendorEmail);
            activityLog.setVendorName(vendorName);

            activityLog.setApproverId(approverId);
            activityLog.setApproverRemark(approverRemark);
            activityLog.setApproverName(approverName);

            activityLog.setInitiatorId(initiatorId);
            activityLog.setInitiatorRemark(initiatorRemark == null ? log.get().getInitiatorRemark(): initiatorRemark);
            activityLog.setInitiatorName(initiatorName);
            activityLog.setStatus(status);

            activityRepo.save(activityLog);

        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
    }


    public Object[] validateFile(Map<String, MultipartFile> fileMap){
        AtomicBoolean valid = new AtomicBoolean(true);

        AtomicReference<String> errMsg = new AtomicReference<>("");
        fileMap.forEach(
                (k, v)->{
                    String ext = fileExt(v.getOriginalFilename());
                    if (!extensions.contains(ext)){
                        valid.set(false);
                        errMsg.set("Invalid file format [ " + Collections.singletonList(extensions) + " ]");
                    }
                }
        );
        return new Object[] {valid.get(), errMsg.get()};
    }

    /**
     * @param staffId of the staff to get the role.
     * @apiNote returns the role of the staff by calling the role management endpoint
     * */
    public JsonNode role(String staffId){
        try{

            ResponseEntity<JsonNode> request = new RestTemplate().exchange(
                    roleMngmtUrl+staffId+"/"+"15",
                    HttpMethod.GET,
                    new HttpEntity<>(staffId),
                    JsonNode.class
            );
            System.out.println("request = "+request);
            System.out.println("response = "+request.getBody());
            if (request.getStatusCodeValue() == 200){
                if (!Objects.requireNonNull(request.getBody()).isEmpty()){
                    return request.getBody();
                }else{
                    return null;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
