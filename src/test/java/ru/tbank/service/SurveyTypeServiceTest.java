package ru.tbank.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.entity.SurveyType;
import ru.tbank.repository.SurveyTypeRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SurveyTypeServiceTest {
    @Mock
    private SurveyTypeRepository surveyTypeRepository;
    @InjectMocks
    private SurveyTypeService surveyTypeService;

    private SurveyType surveyType;

    @BeforeEach
    void setUp() {
        surveyType = new SurveyType();
        surveyType.setSurveyTypeId(1L);
        surveyType.setSurveyTypeName("Обычный");
        surveyType.setQuizFlg(false);
        surveyType.setMultipleChoiceFlg(false);
        surveyType.setInsertDt(LocalDateTime.now());
    }

    @Test
    void testGetAllSurveyTypes_OK() {
        when(surveyTypeRepository.findAll()).thenReturn(List.of(surveyType));
        List<SurveyType> surveyTypes = surveyTypeService.getAllSurveyTypes();

        Assertions.assertAll(
                () -> Assertions.assertFalse(surveyTypes.isEmpty(), "Список типов не пуст"),
                () -> Assertions.assertEquals(1, surveyTypes.size(), "Добавился один тип"),
                () -> Assertions.assertEquals(surveyType.getSurveyTypeName(), surveyTypes.get(0).getSurveyTypeName(), "Добавилось что надо")
        );
    }

    @Test
    void testGetAllSurveyTypes_Empty() {
        when(surveyTypeRepository.findAll()).thenReturn(Collections.emptyList());

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyTypeService.getAllSurveyTypes(), "Список типов пуст");
    }

    @Test
    void testGetSurveyTypeById_OK() {
        when(surveyTypeRepository.findBySurveyTypeId(anyLong())).thenReturn(surveyType);
        SurveyType surveyTypeResult = surveyTypeService.getSurveyTypeById(surveyType.getSurveyTypeId());

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyTypeResult, "Получен результат с типом"),
                () -> Assertions.assertEquals(surveyType.getSurveyTypeId(), surveyTypeResult.getSurveyTypeId(), "Добавляемый и добавленный тип совпадают")
        );
    }

    @Test
    void testGetSurveyTypeById_NotFound() {
        when(surveyTypeRepository.findBySurveyTypeId(anyLong())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyTypeService.getSurveyTypeById(surveyType.getSurveyTypeId()), "Типа с таким ид не существует");
    }

    @Test
    void testGetSurveyTypeByConfig_OK() {
        when(surveyTypeRepository.findByMultipleChoiceFlgAndQuizFlg(anyBoolean(), anyBoolean())).thenReturn(surveyType);
        SurveyType surveyTypeResult = surveyTypeService.getSurveyTypeByConfig(true, false);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(surveyTypeResult, "Получен тип опроса в результате запроса"),
                () -> Assertions.assertEquals(surveyType.getSurveyTypeId(), surveyTypeResult.getSurveyTypeId(), "Добавленный тип совпадает с добавляемым")
        );
    }

    @Test
    void testGetSurveyTypeByConfig_NotFound() {
        when(surveyTypeRepository.findByMultipleChoiceFlgAndQuizFlg(anyBoolean(), anyBoolean())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> surveyTypeService.getSurveyTypeByConfig(true, false), "Тип опроса с такими опциями не найден");
    }
}
