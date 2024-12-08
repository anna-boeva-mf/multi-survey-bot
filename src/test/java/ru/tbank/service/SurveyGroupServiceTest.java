package ru.tbank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.BadRequestException;

import org.springframework.dao.DataIntegrityViolationException;
import ru.tbank.dto.SurveyGroupDTO;
import ru.tbank.entity.SurveyGroup;
import ru.tbank.exception.EntityAlreadyExistsException;
import ru.tbank.repository.SurveyGroupRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SurveyGroupServiceTest {
    @Mock
    private SurveyGroupRepository surveyGroupRepository;
    @InjectMocks
    private SurveyGroupService surveyGroupService;

    private SurveyGroup surveyGroup;
    private SurveyGroupDTO surveyGroupDTO;

    @BeforeEach
    void setUp() {
        surveyGroup = new SurveyGroup();
        surveyGroup.setSurveyGroupId(1L);
        surveyGroup.setSurveyGroupName("example");
        surveyGroup.setInsertDt(LocalDateTime.now());
        surveyGroupDTO = new SurveyGroupDTO();
        surveyGroupDTO.setSurveyGroupName("example");
        surveyGroupDTO.setSurveyTypeId(1L);
    }

    @Test
    void testGetAllSurveyGroups_OK() {
        when(surveyGroupRepository.findAll()).thenReturn(List.of(surveyGroup));
        List<SurveyGroup> result = surveyGroupService.getAllSurveyGroups();

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.isEmpty(), "Результат запроса не пуст"),
                () -> Assertions.assertEquals(1, result.size(), "В результате 1 элемент"),
                () -> Assertions.assertEquals(surveyGroup.getSurveyGroupName(), result.get(0).getSurveyGroupName(), "Проверка наименования группы")
        );
    }

    @Test
    void testGetAllSurveyGroups_EmptyList() {
        when(surveyGroupRepository.findAll()).thenReturn(Collections.emptyList());

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyGroupService.getAllSurveyGroups(), "Пустой список групп запросов");
    }

    @Test
    void testGetSurveyGroupByName_OK() {
        when(surveyGroupRepository.findBySurveyGroupName(anyString())).thenReturn(surveyGroup);
        SurveyGroup surveyGroupResult = surveyGroupService.getSurveyGroupByName(surveyGroup.getSurveyGroupName());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyGroupResult, "Результат получен"),
                () -> Assertions.assertEquals(surveyGroup.getSurveyGroupName(), surveyGroupResult.getSurveyGroupName(), "Проверка наименования группы")
        );
    }

    @Test
    void testGetSurveyGroupByName_NotFound() {
        when(surveyGroupRepository.findBySurveyGroupName(anyString())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyGroupService.getSurveyGroupByName("Nonexistent Group"), "Группа опросов не найдена");
    }

    @Test
    void testCreateSurveyGroup_OK() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(false);
        when(surveyGroupRepository.save(any(SurveyGroup.class))).thenReturn(surveyGroup);
        SurveyGroup surveyGroupResult = surveyGroupService.createSurveyGroup(surveyGroupDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyGroupResult, "Результат не пуст"),
                () -> Assertions.assertEquals(surveyGroup.getSurveyGroupName(), surveyGroupResult.getSurveyGroupName(), "У созанной группы верное наименование")
        );
    }

    @Test
    void testCreateSurveyGroup_EmptyName() {
        surveyGroupDTO.setSurveyGroupName("");

        Assertions.assertThrows(IllegalArgumentException.class, () -> surveyGroupService.createSurveyGroup(surveyGroupDTO), "Ошиба создания группы с пустым наименованием");
    }

    @Test
    void testCreateSurveyGroup_AlreadyExists() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(true);

        Assertions.assertThrows(EntityAlreadyExistsException.class, () -> surveyGroupService.createSurveyGroup(surveyGroupDTO), "Создаваемая группа уже существует");
    }

    @Test
    void testUpdateSurveyGroup_OK() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(true);
        when(surveyGroupRepository.findBySurveyGroupName(anyString())).thenReturn(surveyGroup);
        when(surveyGroupRepository.save(any(SurveyGroup.class))).thenReturn(surveyGroup);
        SurveyGroup surveyGroupResult = surveyGroupService.updateSurveyGroup(surveyGroup.getSurveyGroupName(), surveyGroupDTO);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyGroupResult, "Результат не пуст"),
                () -> Assertions.assertEquals(surveyGroup.getSurveyGroupName(), surveyGroupResult.getSurveyGroupName(), "Проверка наименования у измененной группы")
        );
    }

    @Test
    void testUpdateSurveyGroup_NotFound() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyGroupService.updateSurveyGroup("Nonexistent Group", surveyGroupDTO), "Изменение несуществующей группы опросов");
    }

    @Test
    void testUpdateSurveyGroup_AlreadyExists() {
        surveyGroupDTO.setSurveyGroupName("group");
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(true);

        Assertions.assertThrows(EntityAlreadyExistsException.class, () -> surveyGroupService.updateSurveyGroup(surveyGroup.getSurveyGroupName(), surveyGroupDTO), "Изменяемая группа опросов уже существует");
    }

    @Test
    void testDeleteSurveyGroup_OK() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(true);
        when(surveyGroupRepository.findBySurveyGroupName(anyString())).thenReturn(surveyGroup);
        boolean result = surveyGroupService.deleteSurveyGroup(surveyGroup.getSurveyGroupName());

        Assertions.assertTrue(result);
        verify(surveyGroupRepository, times(1)).deleteById(surveyGroup.getSurveyGroupId());
    }

    @Test
    void testDeleteSurveyGroup_NotFound() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyGroupService.deleteSurveyGroup("Nonexistent Group"), "Удаляемая группа опросов не существует");
    }

    @Test
    void testCreateSurveyGroup_DataIntegrityViolation() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(false);
        when(surveyGroupRepository.save(any(SurveyGroup.class))).thenThrow(new DataIntegrityViolationException("Integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> surveyGroupService.createSurveyGroup(surveyGroupDTO), "Ошибка рабты с базой");
    }

    @Test
    void testUpdateSurveyGroup_DataIntegrityViolation() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(true);
        when(surveyGroupRepository.findBySurveyGroupName(anyString())).thenReturn(surveyGroup);
        when(surveyGroupRepository.save(any(SurveyGroup.class))).thenThrow(new DataIntegrityViolationException("Integrity violation"));

        Assertions.assertThrows(BadRequestException.class, () -> surveyGroupService.updateSurveyGroup(surveyGroup.getSurveyGroupName(), surveyGroupDTO), "Ошибка рабты с базой");
    }

    @Test
    void testDeleteSurveyGroup_DataIntegrityViolation() {
        when(surveyGroupRepository.existsBySurveyGroupName(anyString())).thenReturn(true);
        when(surveyGroupRepository.findBySurveyGroupName(anyString())).thenReturn(surveyGroup);
        doThrow(new DataIntegrityViolationException("Integrity violation")).when(surveyGroupRepository).deleteById(anyLong());

        Assertions.assertThrows(BadRequestException.class, () -> surveyGroupService.deleteSurveyGroup(surveyGroup.getSurveyGroupName()), "Ошибка рабты с базой");
    }
}
