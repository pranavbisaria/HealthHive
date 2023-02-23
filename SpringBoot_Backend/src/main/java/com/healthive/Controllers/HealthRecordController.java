package com.healthive.Controllers;
import com.healthive.Models.User;
import com.healthive.Payloads.ApiResponse;
import com.healthive.Payloads.HealthRecordDto;
import com.healthive.Payloads.PageResponse;
import com.healthive.Security.CurrentUser;
import com.healthive.Service.HealthRecordService;
import com.healthive.Service.StorageServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path ="/api/healthRecord")
@RequiredArgsConstructor
public class HealthRecordController {
    private final HealthRecordService healthRecordService;
    private final StorageServices storageServices;
    @PostMapping("/saveRecord")
    public ResponseEntity<?> saveRecord(@CurrentUser User user, @RequestPart("images") MultipartFile[] images, @Valid @RequestPart HealthRecordDto healthRecordDto) throws Exception {
        if (FileValidation(images))
            return new ResponseEntity<>(new ApiResponse("File is not of image type(JPEG/ JPG or PNG)!!!", false), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        Arrays.stream(images).forEach(multipartFile -> {
            String image = this.storageServices.uploadFile(multipartFile);
            healthRecordDto.getPrescriptionUrls().add(image);
        });
        this.healthRecordService.addPatientRecord(user, user.getSerial(), user.getEmail(), user.getPrivateKey(), healthRecordDto.getName(), healthRecordDto.getLocation(), healthRecordDto.getTo(), healthRecordDto.getFrom(), healthRecordDto.getSymptoms(), healthRecordDto.getPrescriptionUrls(), healthRecordDto.getDescription(), false);
        return new ResponseEntity<>(new ApiResponse("Your health Record Successfully added to the queue", true), OK);
    }
    @GetMapping("/getRecord")
    public ResponseEntity<?> getHealthRecord(@CurrentUser User user, @RequestParam(value ="pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                             @RequestParam(value ="pageSize", defaultValue = "5", required = false) Integer pageSize) throws Exception {
        List<HealthRecordDto> healthRecordDtoList = new ArrayList<>(0);
        boolean lastPage = false;
        int totalElements = user.getSerial().intValue() + 1;
        int totalPage = totalElements/pageSize +1;
        BigInteger Ub = user.getSerial().subtract(user.getSerial().multiply(BigInteger.valueOf(pageNumber)));
        BigInteger i;
        System.out.println("\n\npageNumber:"+pageNumber+"\npageSize:"+pageSize+"\ntotalPage:"+totalPage+"\ntotalElements:"+totalElements+"\nlastPage:"+lastPage);
        for(i = Ub; (i.compareTo(Ub.subtract(BigInteger.valueOf(pageSize)))> 0) && i.compareTo(BigInteger.valueOf(0))>=0; i = i.subtract(BigInteger.ONE)){
            HealthRecordDto healthRecordDto = this.healthRecordService.getPatientRecord(i, user.getEmail(), user.getPrivateKey());
            healthRecordDtoList.add(healthRecordDto);
        }
        if(i.compareTo(BigInteger.valueOf(0))<=0){
            lastPage = true;
        }
        return new ResponseEntity<>(new PageResponse(new ArrayList<>(healthRecordDtoList), pageNumber, pageSize, totalPage, totalElements, lastPage), OK);
    }
    @PutMapping("/updateRecord/{id}")
    public ResponseEntity<?> updateHealthRecord(@CurrentUser User user, @PathVariable("id") BigInteger Id, @RequestPart("images") MultipartFile[] images, @Valid @RequestPart HealthRecordDto healthRecordDto) throws Exception {
        if (FileValidation(images))
            return new ResponseEntity<>(new ApiResponse("File is not of image type(JPEG/ JPG or PNG)!!!", false), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        Arrays.stream(images).forEach(multipartFile -> {
            String image = this.storageServices.uploadFile(multipartFile);
            healthRecordDto.getPrescriptionUrls().add(image);
        });
        this.healthRecordService.addPatientRecord(user, Id, user.getEmail(), user.getPrivateKey(), healthRecordDto.getName(), healthRecordDto.getLocation(), healthRecordDto.getTo(), healthRecordDto.getFrom(), healthRecordDto.getSymptoms(), healthRecordDto.getPrescriptionUrls(), healthRecordDto.getDescription(), true);
        return new ResponseEntity<>(new ApiResponse("Your update request health Record Id:"+Id+" has been successfully added to the queue", true), OK);
    }
    public boolean FileValidation(MultipartFile[] images) throws NullPointerException{
        for (MultipartFile image : images) {
            if (!Objects.equals(image.getContentType(), "image/png") && !Objects.equals(image.getContentType(), "image/jpg") && !Objects.equals(image.getContentType(), "image/jpeg") && !Objects.equals(image.getContentType(), "image/webp")) {
                return true;
            }
        }
        return false;
    }
}
