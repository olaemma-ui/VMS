package com.api.vendor.Controller;

import com.api.vendor.Message.Response;
import com.api.vendor.Model.Temp.TempVendor;
import com.api.vendor.Service.VendorService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = {"suntrust/vendor/api/"})
@CrossOrigin("*")
public class VendorController {
    private final VendorService vendorService;

    @Autowired
    VendorController(VendorService vendorService){
        this.vendorService = vendorService;
    }

    @PostMapping(value = {"staff/vendor/add"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> addVendor(@RequestBody Map<String, String> vendor){
        return vendorService.addVendor(vendor);
    }

    @PostMapping(value = {"vendor/register"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> registerVendor(@RequestBody TempVendor tempVendor){
        return vendorService.vendorRegister(tempVendor);
    }

    @PostMapping(value = {"vendor/save"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> saveVendor(@RequestBody TempVendor tempVendor){
        return vendorService.vendorSave(tempVendor);
    }

    @PostMapping(value = {"staff/vendor/approval"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> approveVendor(@RequestBody JsonNode node){
        return vendorService.approveVendor(node);
    }

    @PostMapping(value = {"staff/vendor/decline"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> declineVendor(@RequestBody JsonNode node){
        return vendorService.approveVendor(node);
    }


    @PostMapping(value = {"staff/vendor/details"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getVendorDetails(@RequestBody JsonNode node){
        return vendorService.getVendorDetails(node, "");
    }


    @PostMapping(value = {"staff/pendingVendor/details"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getPendingVendorDetails(@RequestBody JsonNode node){
        return vendorService.getVendorDetails(node, "pending");
    }

    @GetMapping(value = {"staff/vendor/declined/list"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getDeclinedVendors(){
        return vendorService.getDeclinedVendor();
    }


    @GetMapping(value = {"staff/vendor/pending/list"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getPendingRequest(){
        return vendorService.getPendingRequest();
    }

    @GetMapping(value = {"staff/vendor/list"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getAllVendors(){
        return vendorService.getAllVendors();
    }

    @GetMapping(value = {"staff/vendor/active/list"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getActiveVendors(){
        return vendorService.getActiveVendors();
    }


    @GetMapping(value = {"staff/vendor/blacklist/list"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Response> getBlacklistVendor(){
        return vendorService.getBlacklistVendor();
    }

    @GetMapping(value = "vendor/workingCapital/list")
    private ResponseEntity<Response> getWorkingCapital(){
        return vendorService.workingCapital();
    }



    @GetMapping(value = "vendor/documentType/list")
    private ResponseEntity<Response> getDocumetList(){
        return vendorService.getAllDocuments();
    }

    @GetMapping(value = "vendor/businessType/list")
    private ResponseEntity<Response> getBusinessType(){
        return vendorService.businessTypeList();
    }

    @PostMapping(value = {"staff/vendor/blacklist"})
    private ResponseEntity<Response> blacklistVendor(@RequestBody JsonNode node){
        return vendorService.blacklist$whitelistVendor(node);
    }

    @PostMapping(value = {"staff/vendor/whitelist"})
    private ResponseEntity<Response> whitelistVendor(@RequestBody JsonNode node){
        return vendorService.blacklist$whitelistVendor(node);
    }

    @PutMapping(value = {"staff/vendor/updateDetails"})
    private ResponseEntity<Response> updateVendor(@RequestBody TempVendor vendor){
        return vendorService.updateVendorDetails(vendor);
    }

    @GetMapping(value = {"staff/activityLog"})
    private ResponseEntity<Response> getActivityLog() {
        return vendorService.getActivityLog();
    }

    @PostMapping(value = {"login"})
    private ResponseEntity<Response> login(@RequestBody JsonNode node){
        return vendorService.login(node);
    }

}
