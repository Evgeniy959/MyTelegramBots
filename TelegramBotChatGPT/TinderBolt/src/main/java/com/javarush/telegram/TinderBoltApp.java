package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "JDemoChatGPTBot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7712022201:AAGUrAJiQebjNI9VcXp8QayyhqfQgancRGY"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:FUj5RRFaxyJPx8r99jGlJFkblB3TiwFfStVTSk3zPf89Bss9"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();
    private UserInfo me;
    private UserInfo she;
    private int questCount;
    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;

        }

        //command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }
        if (currentMode == DialogMode.GPT && !isMessageCommand()){
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("ChatGPT  думает над вариантами ответа...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        //command DATE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(
                    text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy"
            );
            return;
        }
        if (currentMode == DialogMode.DATE && !isMessageCommand()){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")){
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор пригласи на свидание ❤\uFE0F за 5 сообщений");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }

            String answer = chatGPT.addMessage(message);
            sendTextMessage(answer);
            return;
        }

        //command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Скиньте в чат вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }
        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")){
                String prompt = loadPrompt(query);
                String userChartHistory = String.join("/n/n", list);
                Message msg = sendTextMessage("ChatGPT  думает над вариантами ответа...");
                String answer = chatGPT.sendMessage(prompt, userChartHistory);
                updateTextMessage(msg, answer);
                return;
            }
            list.add(message);
            return;
        }

        //command PROFILE
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            me = new UserInfo();
            questCount = 1;
            String text = loadMessage("profile");
            sendTextMessage(text);
            sendTextMessage("Сколько вам лет?");
            return;
        }
        if (currentMode == DialogMode.PROFILE && !isMessageCommand()){
            switch (questCount){
                case 1:
                    me.age = message;
                    questCount = 2;
                    sendTextMessage("Кем вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questCount = 3;
                    sendTextMessage("У вас есть хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questCount = 4;
                    sendTextMessage("Что вам НЕ нравиться в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = message;
                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("ChatGPT  думает \uD83E\uDDE0 над вариантами ответа...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
        //command OPENER
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");
            she = new UserInfo();
            questCount = 1;
            String text = loadMessage("opener");
            sendTextMessage(text);
            sendTextMessage("Имя девушки?");
            return;
        }
        if (currentMode == DialogMode.OPENER && !isMessageCommand()){
            switch (questCount){
                case 1:
                    she.name = message;
                    questCount = 2;
                    sendTextMessage("Сколько ей лет");
                    return;
                case 2:
                    she.age = message;
                    questCount = 3;
                    sendTextMessage("Есть ли у нее хобби и какие?");
                    return;
                case 3:
                    she.hobby = message;
                    questCount = 4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation = message;
                    questCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;
                    String aboutMyself = she.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("ChatGPT  думает \uD83E\uDDE0 над вариантами ответа...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
