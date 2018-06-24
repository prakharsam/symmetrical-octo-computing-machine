package com.coviam.codiecon.service;
import com.coviam.codiecon.dto.AlgoInputObject;
import com.coviam.codiecon.dto.CandidateDto;
import com.coviam.codiecon.dto.InterviewerDto;
import com.coviam.codiecon.dto.UploadDto;
import com.coviam.codiecon.model.Candidate;
import com.coviam.codiecon.model.Interviewer;
import com.coviam.codiecon.repository.AdminRepository;
import com.coviam.codiecon.repository.CandidateRepository;
import com.coviam.codiecon.repository.InterviewerRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class UploadServiceImpl implements UploadService{
    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Autowired
    private AdminRepository adminRepository;

    public void batchStoreCandidate(String email,List<Candidate> candidateList) {
        for (int i = 0; i < candidateList.size(); i++) {
            candidateRepository.save(candidateList.get(i));
        }
        List<CandidateDto> candidateDtoList = new ArrayList<>();

        for(int i=0;i<candidateList.size();i++){
            CandidateDto candidateDto = new CandidateDto();
            candidateDto.setEmail(candidateList.get(i).getEmail());
            candidateDto.setPreference(candidateList.get(i).getPreference());
            candidateDto.setPassword(candidateList.get(i).getPassword());
            candidateDto.setDay(candidateList.get(i).getDay());
            candidateDto.setName(candidateList.get(i).getName());
            candidateDtoList.add(candidateDto);
        }

        if(adminRepository.existsById(email)){
            List<AlgoInputObject> algoInputObjectList = adminRepository.findById(email).get().getAlgoInputObjectList();
            algoInputObjectList.get(algoInputObjectList.size()-1).setCandidateDtoList(candidateDtoList);
        }
    }

    public void uploadFileCandidate(String email,MultipartFile multipartFile) throws IOException {
        System.out.println("checking .. ");
        File file = convertMultiPartToFile(multipartFile);
        List<Candidate> candidateArrayList = new ArrayList<>();

        try (Reader reader = new FileReader(file);) {
            @SuppressWarnings("unchecked")
            CsvToBean<UploadDto> csvToBean = new CsvToBeanBuilder<UploadDto>(reader).withType(UploadDto.class)
                    .withIgnoreLeadingWhiteSpace(true).build();
            List<UploadDto> uploadDtos = csvToBean.parse();


            for (UploadDto uploadDto : uploadDtos) {
                System.out.println(uploadDto);
                Candidate candidate = new Candidate();
                candidate.setEmail(uploadDto.getEmail());
                candidate.setName(uploadDto.getName());
                candidate.setDay(0);
                candidate.setPassword(encryptPassword());
                candidate.setPreference("MAEN");
                candidateArrayList.add(candidate);
            }
            batchStoreCandidate(email,candidateArrayList);
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public String encryptPassword(){
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public void batchStoreInterview(String email,List<Interviewer> interviewerList) {
        for (int i = 0; i < interviewerList.size(); i++) {
            interviewerRepository.save(interviewerList.get(i));
        }
        List<InterviewerDto> interviewers = new ArrayList<>();
        for(int i=0;i<interviewerList.size();i++){
            InterviewerDto interviewer = new InterviewerDto();
            interviewer.setName(interviewerList.get(i).getName());
            interviewer.setAvailablityOfInterviewer(interviewerList.get(i).getAvailablityOfInterviewer());
            interviewer.setEmail(interviewerList.get(i).getEmail());
            interviewer.setPassword(interviewerList.get(i).getPassword());
            interviewers.add(interviewer);
        }
        if(adminRepository.existsById(email)){
            List<AlgoInputObject> algoInputObjectList = adminRepository.findById(email).get().getAlgoInputObjectList();
            algoInputObjectList.get(algoInputObjectList.size()-1).setInterviewerDtoList(interviewers);
        }
    }

    public void uploadFileInterview(String email,MultipartFile multipartFile) throws IOException {

        File file = convertMultiPartToFile(multipartFile);
        List<Interviewer> interviewerArrayList = new ArrayList<>();

        try (Reader reader = new FileReader(file);) {
            @SuppressWarnings("unchecked")
            CsvToBean<UploadDto> csvToBean = new CsvToBeanBuilder<UploadDto>(reader).withType(UploadDto.class)
                    .withIgnoreLeadingWhiteSpace(true).build();
            List<UploadDto> uploadDtos = csvToBean.parse();


            for (UploadDto uploadDto:uploadDtos) {
                Interviewer interviewer = new Interviewer();
                interviewer.setEmail(uploadDto.getEmail());
                interviewer.setName(uploadDto.getName());
                interviewer.setPassword(encryptPassword());
                interviewer.setAvailablityOfInterviewer(null);
                interviewerArrayList.add(interviewer);
            }
            batchStoreInterview(email,interviewerArrayList);
        }
    }

}