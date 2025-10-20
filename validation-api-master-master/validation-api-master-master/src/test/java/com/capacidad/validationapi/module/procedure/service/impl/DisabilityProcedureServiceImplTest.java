package com.capacidad.validationapi.module.procedure.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.dto.FileTagDTO;
import com.capacidad.validationapi.module.procedure.model.DisabilityProcedure;
import com.capacidad.validationapi.module.procedure.model.FileTag;
import com.capacidad.validationapi.module.procedure.repository.DisabilityProcedureRepository;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DisabilityProcedureServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DisabilityProcedureRepository disabilityProcedureRepository;

    @Mock
    private StorageService storageService;

    @Spy
    @InjectMocks
    private DisabilityProcedureServiceImpl disabilityProcedureService;


    @Test
    public void testAddFileTagAppendsNewTagSuccessfully() throws ObjectNotFoundException {
        DisabilityProcedure disabilityProcedure = new DisabilityProcedure();

        FileTagDTO fileTagDTO = new FileTagDTO();
        fileTagDTO.setFilename("file.pdf");
        fileTagDTO.setTag("tag");

        FileTag fileTag = new FileTag();
        fileTag.setTag(fileTagDTO.getTag());
        fileTag.setFilename(fileTagDTO.getFilename());

        doReturn(disabilityProcedure).when(disabilityProcedureService).findById(1L);
        when(disabilityProcedureService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(fileTagDTO, FileTag.class)).thenReturn(fileTag);
        when(disabilityProcedureRepository.save(disabilityProcedure)).thenReturn(disabilityProcedure);

        FileTag result = disabilityProcedureService.addFileTag(1L, fileTagDTO);

        assertThat(result).isEqualTo(fileTag);
        assertThat(disabilityProcedure.getFileTags().size()).isEqualTo(1);
    }

    @Test
    public void testAddFileTagReplaceTagForExistentFileSuccessfully() throws ObjectNotFoundException {
        DisabilityProcedure disabilityProcedure = new DisabilityProcedure();

        FileTagDTO fileTagDTO = new FileTagDTO();
        fileTagDTO.setFilename("file.pdf");
        fileTagDTO.setTag("newTag");

        FileTag fileTag = new FileTag();
        fileTag.setTag(fileTagDTO.getTag());
        fileTag.setFilename(fileTagDTO.getFilename());

        FileTag existentTag = new FileTag();
        existentTag.setTag("oldTag");
        existentTag.setFilename(fileTagDTO.getFilename());
        disabilityProcedure.getFileTags().add(existentTag);

        doReturn(disabilityProcedure).when(disabilityProcedureService).findById(1L);
        when(disabilityProcedureService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(fileTagDTO, FileTag.class)).thenReturn(fileTag);
        when(disabilityProcedureRepository.save(disabilityProcedure)).thenReturn(disabilityProcedure);

        FileTag result = disabilityProcedureService.addFileTag(1L, fileTagDTO);

        assertThat(result).isEqualTo(fileTag);
        assertThat(disabilityProcedure.getFileTags().size()).isEqualTo(1);
        assertThat(disabilityProcedure.getFileTags().iterator().next().getTag()).isEqualTo(fileTagDTO.getTag());
    }

    @Test
    public void testListFilesAppendTagsToSummarySuccessfully() throws ObjectNotFoundException, ObjectNotValidException {
        DisabilityProcedure disabilityProcedure = new DisabilityProcedure();

        FileTag fileTag = new FileTag();
        fileTag.setTag("fileTag");
        fileTag.setFilename("file.pdf");

        SummaryDTO summaryDTO = new SummaryDTO();
        summaryDTO.setName(fileTag.getFilename());

        disabilityProcedure.getFileTags().add(fileTag);

        doReturn(disabilityProcedure).when(disabilityProcedureService).findById(1L);
        when(disabilityProcedureService.getStorageService()).thenReturn(storageService);
        when(storageService.getFileList(FileType.BENEFICIARY_PROCEDURE, 1L)).thenReturn(Collections.singletonList(summaryDTO));

        List<SummaryDTO> results = disabilityProcedureService.listFiles(1L);

        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getTag()).isEqualTo(fileTag.getTag());
    }

}
