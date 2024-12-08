package ru.tbank.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tbank.bot.entity.BotSurvey;
import ru.tbank.bot.entity.BotSurveySession;
import ru.tbank.bot.entity.BotPoll;
import ru.tbank.dto.ResultDTO;
import ru.tbank.dto.UserCreateDTO;
import ru.tbank.entity.SurveyGroup;
import ru.tbank.entity.User;
import ru.tbank.service.ResultService;
import ru.tbank.service.SurveyGroupService;
import ru.tbank.service.SurveyService;
import ru.tbank.service.SurveyTypeService;
import ru.tbank.service.UserDetailsServiceImpl;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SurveyBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private UserDetailsServiceImpl userService;
    private SurveyGroupService surveyGroupService;
    private SurveyTypeService surveyTypeService;
    private SurveyService surveyService;
    private ResultService resultService;

    public SurveyBot(
            @Value("${telegram-bot.name}") String botUsername,
            @Value("${telegram-bot.token}") String botToken, UserDetailsServiceImpl userService, SurveyGroupService surveyGroupService,
            SurveyTypeService surveyTypeService, SurveyService surveyService, ResultService resultService) throws TelegramApiException {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.userService = userService;
        this.surveyGroupService = surveyGroupService;
        this.surveyTypeService = surveyTypeService;
        this.surveyService = surveyService;
        this.resultService = resultService;
    }

    private Map<Long, BotSurveySession> sessions = new ConcurrentHashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Chat chat = update.getMessage().getChat();
            long chatId = chat.getId();
            if (messageText.equals("/start")) {
                startSession(chat);
            } else if (messageText.equals("/quit")) {
                startSession(chat);
            } else if (sessions.containsKey(chatId)) {
                handleSurveyResponse(chatId, update);
            } else {
                sendMessage(chatId, "Чтобы выбрать опрос, введите /start");
            }
        } else if (update.hasPollAnswer()) {
            long chatId = update.getPollAnswer().getUser().getId();
            handleSurveyResponse(chatId, update);
        }
    }

    private void startSession(Chat chat) {
        long chatId = chat.getId();
        BotSurveySession session = new BotSurveySession(chatId);
        Long userId = getUserId(chat);
        session.setUserId(userId);
        sessions.put(chatId, session);
        sendMessage(chatId, "Введите название опроса, который хотите пройти.");
        int surveyNamesExampleCount = 3;
        String surveyNamesExample = getSurveyNamesExample(surveyNamesExampleCount);
        if (StringUtils.hasText(surveyNamesExample)) {
            sendMessage(chatId, "Последние " + surveyNamesExampleCount + " созданных опроса:");
            sendMessage(chatId, surveyNamesExample);
        }
    }

    private String getSurveyNamesExample(int surveyNamesExampleCount) {
        List<SurveyGroup> surveyGroups = surveyGroupService.getAllSurveyGroups();
        if (surveyGroups != null) {
            return surveyGroups.stream().sorted((g1, g2) -> g2.getInsertDt().compareTo(g1.getInsertDt()))
                    .limit(surveyNamesExampleCount).map(g -> g.getSurveyGroupName()).collect(Collectors.joining(", "));
        }
        return null;
    }

    private Long getUserId(Chat chat) {
        User user = userService.findTgUserByUsername(new UserCreateDTO(chat.getId().toString(), chat.getFirstName(), chat.getLastName(), chat.getUserName()));
        return user.getUserId();
    }

    private void handleSurveyResponse(long chatId, Update update) {
        BotSurveySession session = sessions.get(chatId);
        if (session.getBotSurvey() == null) {
            try {
                BotSurvey survey = new BotSurvey.BotSurveyBuilder(update.getMessage().getText(), surveyTypeService, surveyGroupService, surveyService).build();
                if (survey.getBotPolls().isEmpty()) {
                    sendMessage(chatId, "Опрос пуст, выберите другой.");
                } else {
                    List<BotPoll> botPoll = survey.getBotPolls();
                    int currentQuestionIndex = 0;
                    while (currentQuestionIndex < botPoll.size() && isPoolPassed(session.getUserId(), botPoll.get(currentQuestionIndex).getSurveyId())) {
                        session.incrementCurrentQuestionIndex();
                        currentQuestionIndex = session.getCurrentQuestionIndex();
                    }
                    if (currentQuestionIndex < botPoll.size()) {
                        sendMessage(chatId, "Опрос начался. Для выхода из опроса введите /quit");
                        session.setBotSurvey(survey);
                        sendNextQuestion(chatId, session);
                    } else {
                        sendMessage(chatId, "Вы уже ответили на все вопросы. Спасибо за участие!");
                        sessions.remove(chatId);
                        sendMessage(chatId, "Чтобы выбрать другой опрос, введите /start");
                    }
                }
            } catch (EntityNotFoundException e) {
                sendMessage(chatId, "Такого опроса не существут, выберите другой.\nЧтобы выбрать опрос, введите /start");
            }
        } else {
            BotSurvey botSurvey = session.getBotSurvey();
            List<BotPoll> botPoll = botSurvey.getBotPolls();
            int currentQuestionIndex = session.getCurrentQuestionIndex();
            while (currentQuestionIndex < botPoll.size() && isPoolPassed(session.getUserId(), botPoll.get(currentQuestionIndex).getSurveyId())) {
                session.incrementCurrentQuestionIndex();
                currentQuestionIndex = session.getCurrentQuestionIndex();
            }
            if (0 <= currentQuestionIndex - 1 && currentQuestionIndex - 1 < botPoll.size()) {
                saveUserResult(session, update, botPoll.get(currentQuestionIndex - 1));
            }
            if (currentQuestionIndex < botPoll.size()) {
                sendNextQuestion(chatId, session);
            } else {
                sendMessage(chatId, "Вы ответили на все вопросы. Спасибо за участие!");
                sessions.remove(chatId);
                sendMessage(chatId, "Чтобы выбрать другой опрос, введите /start");
            }
        }
    }

    private boolean isPoolPassed(Long userId, Long surveyId) {
        ResultDTO emptyUserResultDTO = new ResultDTO(userId, surveyId, null);
        if (resultService.checkResultExists(emptyUserResultDTO)) {
            return true;
        }
        return false;
    }

    private void saveUserResult(BotSurveySession session, Update update, BotPoll botPoll) {
        if (update.hasPollAnswer()) {
            List<Integer> optionIds = update.getPollAnswer().getOptionIds();
            String answerIds = optionIds.stream().map(o -> botPoll.getOptions().get(o).getAnswerId()).toList().toString();
            ResultDTO userResultDTO = new ResultDTO(session.getUserId(), botPoll.getSurveyId(), answerIds);
            resultService.createResult(userResultDTO);
        }
    }

    private void sendNextQuestion(long chatId, BotSurveySession session) {
        BotSurvey survey = session.getBotSurvey();
        List<BotPoll> botPolls = survey.getBotPolls();
        try {
            if (botPolls != null) {
                BotPoll botPoll = botPolls.get(session.getCurrentQuestionIndex());
                SendPoll sendPoll = new SendPoll();
                sendPoll.setChatId(chatId);
                sendPoll.setQuestion(botPoll.getQuestion());
                sendPoll.setOptions(botPoll.getOptions().stream().map(o -> o.getAnswer()).toList());
                sendPoll.setIsAnonymous(false);
                if (botPoll.isQuizFlg()) {
                    sendPoll.setType("quiz");
                    sendPoll.setCorrectOptionId(botPoll.getCorrectAnswer());
                }
                if (botPoll.isMultipleChoiceFlg()) {
                    sendPoll.setAllowMultipleAnswers(true);
                }
                execute(sendPoll);
                session.incrementCurrentQuestionIndex();
            }
        } catch (Exception e) {
            log.error("Ошибка подготовки вопроса: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
