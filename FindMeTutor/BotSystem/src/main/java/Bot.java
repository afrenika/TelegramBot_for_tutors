
import org.apache.commons.lang3.ArrayUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {

    //активатор функции отправки пароля по почте
    private final boolean mailActivation = false;

    private final String url = "";
    private final String username = "";
    private final String password = "";

    String[] disciplines = new String[]{"русский язык","биология","математика","география","литература",
            "история","обществознание","английский язык","немецкий язык","китайский язык"};
    RegistrationSystem registrationSystem = new RegistrationSystem();
    LoginSystem loginSystem = new LoginSystem();
    private final Map<Long, User> userMap = new HashMap<>();
    private final Map<Long, Client> clientMap = new HashMap<>();
    private final Map<Long, Tutor> tutorMap = new HashMap<>();
    private final Map<Long, Admin> adminMap = new HashMap<>();
    private final Map<Long, Request> savedRequestMap = new HashMap<>();
    
    private final Map<Long, Request> requestMap = new HashMap<>();
    private final Map<Long, Account> edited = new HashMap<>();
    private final Map<Long, Search_criteria> editedCriteria = new HashMap<>();


    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()){
            var msg = update.getMessage();
            var user = msg.getFrom();
            var id= user.getId();

            User user_bot = userMap.get(id);
            if(user_bot == null) {
                user_bot = new User();
                get_role(check_auth(id), user_bot, null);
                userMap.put(user.getId(), user_bot);
            }

            if(msg.isCommand()){
                if(msg.getText().equals("/start")){
                    start_command(id, user_bot);
                    return;
                }
            }
            if(msg.getText().equals("Меню")){
                get_menu(id, user_bot);
                return;
            }
            if(msg.getText().equals("Зарегистрироваться")){
                registrationSystem.registration_0(id, user_bot);
                return;
            }
            if(msg.getText().equals("Получить статистику")){
                if(user_bot.role.equals("admin")){
                    getStatistics(id, user_bot);}
                return;
            }

            if(msg.getText().equals("Войти")){
                loginSystem.login_0(id, user_bot);
                return;
            }
            if(msg.getText().equals("Выйти из аккаунта")){
                switch (user_bot.role) {
                    case "client", "tutor", "admin" -> Account.updateId_account(url, username, password, user_bot.id_account, null);
                }
                user_bot.role = "none";
                user_bot.id_account = 0L;
                get_menu(id, user_bot);
                return;
            }

            if(msg.getText().equals("Искать репетитора")){
                get_tutors(id, user_bot);
                return;
            }
            if(msg.getText().equals("Просмотр заявок")){
                getRequests(id, user_bot.id_account, user_bot);
                return;
            }
            if(msg.getText().equals("Редактировать критерии поиска")){
                if(user_bot.role.equals("client")){
                    editedCriteria.put(user_bot.id_account, clientMap.get(user_bot.id_account).getSearch_criteria().clone());
                    getEditCriteria(id, user_bot);
                }
                else if(user_bot.role.equals("admin")){
                    editedCriteria.put(user_bot.id_account, adminMap.get(user_bot.id_account).getSearch_criteria().clone());
                    getEditCriteria(id, user_bot);
                }
                return;
            }
            if(msg.getText().equals("Редактировать профиль")){
                user_bot.stage = "edit_profile";
                if(user_bot.role.equals("client"))
                    edited.put(user_bot.id_account, clientMap.get(user_bot.id_account).clone());
                if(user_bot.role.equals("tutor")){
                    edited.put(user_bot.id_account, tutorMap.get(user_bot.id_account).clone());
                }
                edit_profile_command(id, user_bot, false);
                return;
            }

            if(user_bot.stage != null){
                if(user_bot.stage.equals("registration")){
                    if(user_bot.step == 2){
                        registrationSystem.registration_2(id, user_bot, msg.getText());}
                    else if(user_bot.step == 3){
                        registrationSystem.registration_3(id,  user_bot, msg.getText());}
                    else if(user_bot.step == 4){
                        registrationSystem.registration_4(id, user_bot, msg.getText());}
                    else if(user_bot.step == 5){
                        registrationSystem.registration_5(id, user_bot, msg.getText());}
                    else if(user_bot.step == 7){
                        registrationSystem.registration_7(id,  user_bot, msg.getText());}
                    else if(user_bot.step == 8){
                        registrationSystem.registration_8(id, user_bot, msg.getText());}
                }
                if(user_bot.stage.equals("login")) {
                    if(user_bot.step == 1){
                        loginSystem.login_1(id, user_bot, msg.getText());}
                    else if(user_bot.step == 2){
                        loginSystem.login_2(id, user_bot, msg.getText());}
                }
                if(user_bot.stage.equals("edit_profile")){
                    if(user_bot.dop_info.contains("change")){
                        if(user_bot.dop_info.equals("change_pas1")){
                            boolean flag = false;
                            if(user_bot.role.equals("tutor")){
                                Tutor tutor = tutorMap.get(user_bot.id_account);
                                flag = (msg.getText().equals(tutor.getPassword()));
                            }
                            if(user_bot.role.equals("client")){
                                Client client = clientMap.get(user_bot.id_account);
                                flag = (msg.getText().equals(client.getPassword()));
                            }
                            if(flag){
                                sendText(id, "Введите новый пароль", user_bot.role);
                                user_bot.dop_info = "change_pas2";}
                            else {
                                user_bot.step++;
                                if(user_bot.step != 3){
                                    sendText_inline(id, "Пароли не совпадают. Попробуйте снова. Попытка №" + user_bot.step+1, user_bot.role, InlineButtons.setInline_password());}
                                else {
                                    sendText(id, "Пароли не совпадают. Попробуйте снова позже.", user_bot.role);
                                    edit_profile_command(id, user_bot, true);
                                    user_bot.dop_info = null;
                                }
                            }
                        }
                        else {
                            switch (user_bot.dop_info) {
                                case "change_fam":
                                    if (user_bot.role.equals("tutor")) {
                                        ((Tutor) edited.get(user_bot.id_account)).setSurname(msg.getText());
                                    } else if (user_bot.role.equals("client")) {
                                        ((Client) edited.get(user_bot.id_account)).setSurname(msg.getText());
                                    }
                                    break;
                                case "change_name":
                                    if (user_bot.role.equals("tutor")) {
                                        ((Tutor) edited.get(user_bot.id_account)).setName(msg.getText());
                                    } else if (user_bot.role.equals("client")) {
                                        ((Client) edited.get(user_bot.id_account)).setName(msg.getText());
                                    }
                                    break;
                                case "change_pas2":
                                    edited.get(user_bot.id_account).setPassword(msg.getText());
                                    break;
                                case "change_date":
                                    ((Tutor) edited.get(user_bot.id_account)).setDate_of_birth(LocalDate.parse(msg.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                                    break;
                                case "change_education":
                                    ((Tutor) edited.get(user_bot.id_account)).setEducation(msg.getText());
                                    break;
                                case "change_city":
                                    ((Tutor) edited.get(user_bot.id_account)).setCity(msg.getText());
                                    break;
                                case "change_exp":
                                    ((Tutor) edited.get(user_bot.id_account)).setWork_experience((int) Double.parseDouble(msg.getText()));
                                    break;
                                case "change_dopInfo":
                                    ((Tutor) edited.get(user_bot.id_account)).setDopInfo(msg.getText());
                                    break;
                                case "change_discipline":
                                    Tutor tutor = ((Tutor) edited.get(user_bot.id_account));
                                    if (!ArrayUtils.contains(tutor.getDisciplines(), msg.getText())) {
                                        String[] array = ArrayUtils.add(tutor.getDisciplines(), msg.getText());
                                        tutor.setDisciplines(array);
                                    }
                                    break;
                            }
                            user_bot.dop_info = null;
                            edit_profile_command(id, user_bot, true);

                    }}
                }
                if(user_bot.stage.equals("request")){
                    if(user_bot.dop_info != null && user_bot.dop_info.equals("dop_info")){
                        Request request = requestMap.get(user_bot.id_account);
                        if(request != null){
                            request.setDopInfo(msg.getText());
                            sendText_inline(id, "Составляем заявку для репетитора.\n" + request.client_request(), user_bot.role, InlineButtons.setInline_request(false, false));
                        }
                        else{
                            sendText(id, "Приносим извинения. Произошла какая-то ошибка, загляните позже.", user_bot.role);
                            get_menu(id, user_bot);
                        }
                    }
                }
                if(user_bot.stage.equals("editCriteria")){
                    if(user_bot.dop_info != null){
                        Search_criteria criteria = editedCriteria.get(user_bot.id_account);
                        criteria.setCriteria(user_bot.dop_info, msg.getText());
                        user_bot.dop_info = null;
                        getEditCriteria(id, user_bot);
                    }
                }
            }


        }
        else if (update.hasCallbackQuery()){
            var msg = update.getCallbackQuery().getMessage();
            var callback = update.getCallbackQuery().getData();
            var id= msg.getChatId();

            User user_bot = userMap.get(id);
            if(user_bot == null) {
                user_bot = new User();
                get_role(check_auth(id), user_bot, null);}

            if(user_bot.stage != null){
                switch (user_bot.stage) {
                    case "registration":
                        if (user_bot.step == 1) {
                            user_bot.dop_info = callback;
                            registrationSystem.registration_1(id, user_bot);}
                        else if (user_bot.step == 6) {
                            registrationSystem.registration_6(id, user_bot, callback);}
                        break;
                    case "login":
                        if(mailActivation){
                            if(callback.equals("check_password")){
                                changePassword(user_bot.dop_info, SendEmail.sendPassword(user_bot.dop_info));
                                editMessage(id, "Новый временный пароль направлен на указанною почту. \nПожалуйста введите пароль", msg.getMessageId());
                            }}
                        break;
                    case "tutor_search":
                        switch (callback) {
                            case "Предыдущий" -> {
                                user_bot.place--;
                                if (user_bot.place >= 0) {
                                    editMessage_inline(id, tutor_card(user_bot.tutors.get(user_bot.place)), msg.getMessageId(), InlineButtons.setInline_tutors(user_bot.tutors.get(user_bot.place), (user_bot.place - 1 >= 0), (user_bot.place + 1 < user_bot.tutors.size())));}}
                            case "Следующий" -> {
                                user_bot.place++;
                                if (user_bot.place < user_bot.tutors.size()) {
                                    editMessage_inline(id, tutor_card(user_bot.tutors.get(user_bot.place)), msg.getMessageId(), InlineButtons.setInline_tutors(user_bot.tutors.get(user_bot.place), (user_bot.place - 1 >= 0), (user_bot.place + 1 < user_bot.tutors.size())));}}
                        }
                        if (callback.contains("Открыть профиль")) {
                            Tutor tutor = user_bot.getTutorById(Long.parseLong(callback.split("-")[1]));
                            if (tutor != null) {
                                if(user_bot.role.equals("client")){
                                    sendText_inline(id, tutor_profile(tutor, false), user_bot.role, InlineButtons.setInline_tutor_profile(tutor));}
                                else if(user_bot.role.equals("admin")){
                                    sendText(id, tutor_profile(tutor, false), user_bot.role);
                                }
                            } else {
                                sendText(id, "Приносим свои извинения. Данная функция недоступна.\nВозвращаем вас в главное меню.", user_bot.role);
                                get_menu(id, user_bot);
                            }
                        }
                        if(callback.contains("Оставить заявку")){
                            if(requestMap.get(user_bot.id_account) == null || callback.contains("delete")){
                                user_bot.stage = "request";
                                Tutor tutor = user_bot.getTutorById(Long.parseLong(callback.split("_")[1]));
                                if (tutor != null) {
                                    Request request = new Request(user_bot.id_account, Long.parseLong(callback.split("_")[1]), clientMap.get(user_bot.id_account));
                                    requestMap.put(user_bot.id_account, request);
                                    if(callback.contains("delete")){editMessage_inline(id, "Составляем заявку для репетитора.\n" + request.client_request(), msg.getMessageId(), InlineButtons.setInline_request(false, false));}
                                    else{sendText_inline(id, "Составляем заявку для репетитора.\n" + request.client_request(), user_bot.role, InlineButtons.setInline_request(false, false));}
                                }
                                else {
                                    sendText(id, "Приносим свои извинения. Данная функция недоступна.\nВозвращаем вас в главное меню.", user_bot.role);
                                    get_menu(id, user_bot);}}
                            else if(callback.contains("continue")){
                                editMessage_inline(id, "Составляем заявку для репетитора.\n" + requestMap.get(user_bot.id_account).client_request(), msg.getMessageId(), InlineButtons.setInline_request(false, false));}
                            else {
                                sendText_inline(id, "В данный момент вы уже имеете одну не сохраненную заявку. Удалить ее и создать новую или продолжить заполнять существующую заявку?", user_bot.role, InlineButtons.setInline_request_correct(callback));}
                        }
                        break;
                    case "edit_profile":
                        if (user_bot.dop_info == null) {
                            switch (callback) {
                                case "Фамилия" -> {
                                    String message = "*Введите фамилию*";
                                    sendText(id, message, user_bot.role);
                                    user_bot.dop_info = "change_fam";
                                }
                                case "Пароль" -> {
                                    String message = "*Введите нынешний пароль*";
                                    sendText_inline(id, message, user_bot.role, InlineButtons.setInline_password());
                                    user_bot.step = 0;
                                    user_bot.dop_info = "change_pas1";
                                }
                                case "Имя" -> {
                                    String message = "*Введите имя*";
                                    sendText(id, message, user_bot.role);
                                    user_bot.dop_info = "change_name";
                                }
                                case "Сохранить" -> {
                                    save_profile(user_bot);
                                    String message = "*Изменения в профиле сохранены*";
                                    sendText(id, message, user_bot.role);
                                    user_bot.dop_info = null;
                                    get_menu(id, user_bot);
                                }

                            }
                            if (user_bot.role.equals("tutor")) {
                                switch (callback) {
                                    case "Пол" -> {
                                        String message = "*Введите пол*\nВоспользуйтесь кнопками ниже";
                                        sendText_inline(id, message, user_bot.role, InlineButtons.setInline_gender());
                                        user_bot.dop_info = "change_gender";
                                    }
                                    case "Дата рождения" -> {
                                        String message = "*Введите дату рождения в формате 01.01.2001*";
                                        sendText(id, message, user_bot.role);
                                        user_bot.dop_info = "change_date";
                                    }
                                    case "Образование" -> {
                                        String message = "*Введите информацию о своем образовании*";
                                        sendText(id, message, user_bot.role);
                                        user_bot.dop_info = "change_education";
                                    }
                                    case "Опыт работы" -> {
                                        String message = "*Введите опыт работы*";
                                        sendText(id, message, user_bot.role);
                                        user_bot.dop_info = "change_exp";
                                    }
                                    case "Дополнительная информация" -> {
                                        String message = "*Введите дополнительную информация о себе*";
                                        sendText(id, message, user_bot.role);
                                        user_bot.dop_info = "change_dopInfo";
                                    }
                                    case "Город" -> {
                                        String message = "*Введите город проживания*";
                                        sendText(id, message, user_bot.role);
                                        user_bot.dop_info = "change_city";
                                    }
                                    case "Дисциплины" -> {
                                        String message = "*Чтобы удалить дисциплину нажмите на кнопку с ее названием, чтобы добавить новую дисциплину нажмите на кнопку с плюсом.*";
                                        sendText_inline(id, message, user_bot.role, InlineButtons.setInline_disciplines(((Tutor) edited.get(user_bot.id_account)).getDisciplines()));
                                        user_bot.dop_info = "change_discipline";
                                    }
                                }
                            } else if (user_bot.role.equals("client")) {
                                if (callback.equals("Статус")) {
                                    String message = "*Введите нынешний статус*\nВоспользуйтесь кнопками ниже";
                                    sendText_inline(id, message, user_bot.role, InlineButtons.setInline_status());
                                    user_bot.dop_info = "change_status";
                                }
                            }
                        }
                        else if(user_bot.dop_info.equals("back_password")){
                            edit_profile_command(id, user_bot, true);
                            user_bot.dop_info = null;
                        }
                        else if (user_bot.dop_info.equals("change_discipline")) {
                            if (callback.equals("add_discipline")) {
                                String message = "*Чтобы добавить новую дисциплину нажмите на кнопку с ее названием или введите ее название.*";
                                sendText_inline(id, message, user_bot.role, InlineButtons.setInline_new_disciplines(disciplines));
                            } else if (callback.contains("new")) {
                                Tutor tutor = ((Tutor) edited.get(user_bot.id_account));
                                if (!ArrayUtils.contains(tutor.getDisciplines(), callback.split("_")[1])) {
                                    String[] array = ArrayUtils.add(tutor.getDisciplines(), callback.split("_")[1]);
                                    tutor.setDisciplines(array);
                                }
                                edit_profile_command(id, user_bot, true);
                                user_bot.dop_info = null;}
                            else {
                                Tutor tutor = ((Tutor) edited.get(user_bot.id_account));
                                String[] array = ArrayUtils.remove(tutor.getDisciplines(), Arrays.binarySearch(tutor.getDisciplines(), callback.split("_")[1]));
                                tutor.setDisciplines(array);
                                edit_profile_command(id, user_bot, true);
                                user_bot.dop_info = null;}
                        }
                        else if (user_bot.dop_info.contains("change")) {
                            if (user_bot.dop_info.equals("change_gender")) {
                                ((Tutor) edited.get(user_bot.id_account)).setGender(callback);}
                            if (user_bot.dop_info.equals("change_status")) {
                                ((Client) edited.get(user_bot.id_account)).setState(callback);}

                            edit_profile_command(id, user_bot, true);
                            user_bot.dop_info = null;
                        }
                        break;
                    case "request":
                        Request request = requestMap.get(user_bot.id_account);
                        if(request != null){
                            if(callback.contains("Дополнительная информация")){
                                user_bot.dop_info = "dop_info";
                                sendText(id, "Введите описание желаемой услуги, подробно опишите свою проблему и важные моменты обучения.", user_bot.role);
                            }
                            else if(callback.contains("Даты")){
                                editMessage_inline(id, "Выберите день недели (заполнить заявку можно только на ближайшую неделю, начиная с завтрашнего дня)"+request.getLocalDateTimes_st(), msg.getMessageId(), InlineButtons.setInline_request(true, false));
                                }
                            else if(callback.contains("delete_time")){
                                request.delete_time(LocalDate.parse(user_bot.dop_info));
                                editMessage_inline(id, "Выберите время (желательно выбрать несколько)\n"+request.getLocalDateTimes_st(LocalDate.parse(user_bot.dop_info).getDayOfWeek().getValue()), msg.getMessageId(), InlineButtons.setInline_request(true, true));

                            }
                            else if(callback.contains("delete_date")){
                                request.delete_date();
                                editMessage_inline(id, "Выберите день недели (заполнить заявку можно только на ближайшую неделю, начиная с завтрашнего дня)\n"+request.getLocalDateTimes_st(), msg.getMessageId(), InlineButtons.setInline_request(true, false));

                            }
                            else if(callback.contains("date")){
                                user_bot.dop_info = callback.split("_")[1];
                                editMessage_inline(id, "Выберите время (желательно выбрать несколько)\n"+request.getLocalDateTimes_st(LocalDate.parse(callback.split("_")[1]).getDayOfWeek().getValue()), msg.getMessageId(), InlineButtons.setInline_request(true, true));
                            }
                            else if(callback.contains("time")){
                                LocalDateTime localDateTime = LocalDateTime.parse(user_bot.dop_info + " " + callback.split("_")[1]+":00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                if(!request.getLocalDateTimes().contains(localDateTime))
                                request.getLocalDateTimes().add(LocalDateTime.parse(user_bot.dop_info + " " + callback.split("_")[1]+":00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                                editMessage_inline(id, "Выберите время (желательно выбрать несколько)\n"+request.getLocalDateTimes_st(LocalDate.parse(user_bot.dop_info).getDayOfWeek().getValue()), msg.getMessageId(), InlineButtons.setInline_request(true, true));
                            }

                            else if(callback.contains("back")){
                                editMessage_inline(id, "Составляем заявку для репетитора.\n" + request.client_request(), msg.getMessageId(), InlineButtons.setInline_request(false, false));
                            }
                            else if(callback.contains("Отправить")){
                                saveRequest(id, user_bot, request);
                            }

                        }
                        break;

                case "editCriteria":
                    if(callback.contains("criteria")){
                        Search_criteria search_criteria = editedCriteria.get(user_bot.id_account);
                        if(callback.equals("criteria_clear")){
                            search_criteria.setNull();
                            editMessage(id, "Критерии очищены", msg.getMessageId());
                            getEditCriteria(id, user_bot);}
                        else if(callback.equals("criteria_save")){
                            saveCriteria(id, user_bot, search_criteria, msg.getMessageId());
                        }
                        else {
                            editMessage_inline(id, "Прежнее значение:\n"+search_criteria.getValue(callback.split("_")[1])+"\nВыберите удалить или изменить критерий",msg.getMessageId(), InlineButtons.setInline_editCriteria2(callback.split("_")[1]));}
                    }
                    else if (callback.contains("editCriteria")) {
                        if (callback.split("_")[1].equals("discipline")) {
                            Search_criteria search_criteria = editedCriteria.get(user_bot.id_account);
                            editMessage_inline(id, "Чтобы удалить дисциплину нажмите на кнопку с ее названием, чтобы добавить новую дисциплину нажмите на кнопку с плюсом.", msg.getMessageId(), InlineButtons.setInline_disciplines(search_criteria.getDisciplines()));
                        }
                        else if (callback.split("_")[1].equals("gender")) {
                            editMessage_inline(id, "Выберите пол", msg.getMessageId(), InlineButtons.setInline_gender());
                        } else {
                            user_bot.dop_info = callback.split("_")[1];
                            sendText(id, "Введите новое значение", user_bot.role);
                        }
                    }
                    else if (callback.contains("deleteCriteria")){
                        editedCriteria.get(user_bot.id_account).deleteCriteria(callback.split("_")[1]);
                        editMessage(id, "Критерий удален", msg.getMessageId());
                        getEditCriteria(id, user_bot);
                    }
                    else if(callback.equals("Мужской")||callback.equals("Женский")){
                        Search_criteria search_criteria = editedCriteria.get(user_bot.id_account);
                        search_criteria.setGender(callback);
                        editMessage(id, "Изменение зафиксировано", msg.getMessageId());
                        getEditCriteria(id, user_bot);
                        user_bot.dop_info = null;
                    }
                    else if (callback.equals("add_discipline")) {
                        String message = "Чтобы добавить новую дисциплину нажмите на кнопку с ее названием или введите ее название.";
                        user_bot.dop_info = "discipline";
                        editMessage_inline(id, message, msg.getMessageId(), InlineButtons.setInline_new_disciplines(disciplines));
                    } else if (callback.contains("new")) {
                        Search_criteria search_criteria = editedCriteria.get(user_bot.id_account);
                        if (!ArrayUtils.contains(search_criteria.getDisciplines(), callback.split("_")[1])) {
                            String[] array = ArrayUtils.add(search_criteria.getDisciplines(), callback.split("_")[1]);
                            search_criteria.setDisciplines(array);
                        }
                        editMessage(id, "Изменение зафиксировано", msg.getMessageId());
                        getEditCriteria(id, user_bot);
                        user_bot.dop_info = null;}
                    else if(callback.contains("delete")){
                        Search_criteria search_criteria = editedCriteria.get(user_bot.id_account);
                        String[] array = ArrayUtils.remove(search_criteria.getDisciplines(), Arrays.binarySearch(search_criteria.getDisciplines(), callback.split("_")[1]));
                        search_criteria.setDisciplines(array);
                        editMessage(id, "Изменение зафиксировано", msg.getMessageId());
                        getEditCriteria(id, user_bot);
                        user_bot.dop_info = null;;
                    }
                    break;}
            }

            else {
                if(callback.contains("заявку")){
                    boolean flag = callback.contains("принять заявку");
                    requestCancelOK(Long.parseLong(callback.split("_")[1]), user_bot, flag);
                    editMessage(id, "Ответ на заявку отправлен", msg.getMessageId());
                }
                else if(callback.contains("deleteRequest")){
                    deleteRequest(Long.parseLong(callback.split("_")[1]), user_bot.role);
                    editMessage(id, "Ответ на заявку зафиксирован", msg.getMessageId());
                }

            }
        }}


    public void editMessage_inline(Long who, String what, Integer msgId,InlineKeyboardMarkup inlineKeyboardMarkup){
        EditMessageText sm = EditMessageText.builder()
                .chatId(who.toString())
                .messageId(msgId)
                .text(what)
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(inlineKeyboardMarkup).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
    public void editMessage(Long who, String what, Integer msgId){
        EditMessageText sm = EditMessageText.builder()
                .chatId(who.toString())
                .messageId(msgId)
                .text(what)
                .parseMode(ParseMode.MARKDOWN)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
    public void sendText(Long who, String what, String role){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what)
                .parseMode(ParseMode.MARKDOWN).build();
        setButtons_menu(sm, role);
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
    public void sendText_inline(Long who, String what, String role, InlineKeyboardMarkup inlineKeyboardMarkup){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what)
                .parseMode(ParseMode.MARKDOWN)
                .build();
        setButtons_menu(sm, role);
        sm.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sm);
            //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
    public void sendPhoto(Long who, File file){
        SendPhoto sendPhoto = SendPhoto.builder().chatId(who.toString()).photo(new InputFile(file)).build();
        try {
            execute(sendPhoto);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public synchronized void setButtons_menu(SendMessage sendMessage, String role) {
        // Создаем клавиатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow1 = new KeyboardRow();
        KeyboardRow keyboardFirstRow2 = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow1.add(new KeyboardButton("Меню"));
        if(role.equals("none")){
            keyboardFirstRow1.add(new KeyboardButton("Войти"));
            keyboardFirstRow1.add(new KeyboardButton("Зарегистрироваться"));
        }
        if(role.equals("admin")){
            keyboardFirstRow1.add(new KeyboardButton("Получить статистику"));
            keyboardFirstRow2.add(new KeyboardButton("Редактировать критерии поиска"));
            keyboardFirstRow2.add(new KeyboardButton("Искать репетитора"));
            keyboardFirstRow2.add(new KeyboardButton("Выйти из аккаунта"));

        }
        if(role.equals("client")){
            keyboardFirstRow1.add(new KeyboardButton("Редактировать критерии поиска"));
            keyboardFirstRow1.add(new KeyboardButton("Искать репетитора"));
            keyboardFirstRow2.add(new KeyboardButton("Редактировать профиль"));
            keyboardFirstRow2.add(new KeyboardButton("Просмотр заявок"));
            keyboardFirstRow2.add(new KeyboardButton("Выйти из аккаунта"));

        }
        if(role.equals("tutor")){
            keyboardFirstRow1.add(new KeyboardButton("Редактировать профиль"));
            keyboardFirstRow2.add(new KeyboardButton("Просмотр заявок"));
            keyboardFirstRow2.add(new KeyboardButton("Выйти из аккаунта"));
        }
        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow1);
        if(keyboardFirstRow2.size() != 0){
        keyboard.add(keyboardFirstRow2);}
        // и устанавливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public void edit_profile_command(Long id, User user_bot, boolean flag){
        if (user_bot.role.equals("client")){
            String dop = client_profile((Client) edited.get(user_bot.id_account), flag);
            sendText_inline(id, dop, user_bot.role, InlineButtons.setInline_profile(user_bot.role));
        }
        else if(user_bot.role.equals("tutor")){
            Tutor tutor = (Tutor) edited.get(user_bot.id_account);
            String dop = tutor_profile(tutor, flag);
            sendText_inline(id, dop, user_bot.role, InlineButtons.setInline_profile(user_bot.role));
        }
    }
    public void start_command(Long id, User user){
        String message = "Добро пожаловать в нашего Telegram бота для поиска репетиторов и клиентов! \nДорогие клиенты, мы предлагаем удобный и быстрый способ нахождения подходящего преподавателя. Мы уверены, что наш бот станет надежным помощником в Вашем образовательном процессе!\nДорогие репетиторы, этот бот - ваш надежный помощник в поиске новых клиентов. Мы поможем вам найти учеников и расширить аудиторию, привлекательно представив ваш профиль и услуги. Чтобы пользоваться функциями бота вы можете воспользоваться кнопками, но перед полноценной работой необходимо пройти авторизацию.";
        sendText(id, message, user.role);
        user.stage = null;
        user.step = 0;
    }
    public void get_menu(Long id, User user){
        String message = "Вы в главном меню Telegram бота для поиска клиентов репетиторам и поиска репетиторов клиентам.";
        sendText(id, message, user.role);
        user.stage = null;
        user.step = 0;
        user.dop_info = null;
    }
    public void get_tutors(Long id, User user){
        String message = "Вы перешли в режим просмотра репетиторов.";
        sendText(id, message, user.role);
        get_tutors_in_map(user);
        if(user.tutors.size() == 0){
            message = "К сожалению, список пуст. Попробуйте смягчить критерии поиска или загляните сюда позже.";
            sendText(id, message, user.role);
            get_menu(id, user);
        }
        else {
            user.stage = "tutor_search";
            user.place = 0;
            sendText_inline(id, tutor_card(user.tutors.get(user.place)), user.role, InlineButtons.setInline_tutors(user.tutors.get(0), (user.place - 1 >= 0), (user.place + 1 < user.tutors.size())));

        }
    }
    public void save_profile(User user){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "UPDATE public.account SET ";
            String acc = "";
            Account account = (user.role.equals("client"))?clientMap.get(user.id_account):tutorMap.get(user.id_account);
            Account account2 = edited.get(user.id_account);
            acc = account.getChanges_ac(account2);
            if(acc.length() != 0){
                sql = sql + acc + " WHERE idaccount = " + user.id_account;
                try {
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(sql);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }}
            sql = "UPDATE " + user.role + " SET ";

            if(user.role.equals("client"))
                acc = clientMap.get(user.id_account).getChanges_c((Client) edited.get(user.id_account));

            else if(user.role.equals("tutor"))
                acc = tutorMap.get(user.id_account).getChanges_c((Tutor) edited.get(user.id_account));

            if(acc.length() != 0){
                sql = sql + acc + " WHERE account_idaccount = " + user.id_account;
                try {
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(sql);
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }}
        }

        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}


        if(user.role.equals("client")){
            clientMap.put(user.id_account, (Client) edited.get(user.id_account));
        }
        else if(user.role.equals("tutor")){
            tutorMap.put(user.id_account, (Tutor) edited.get(user.id_account));
        }
    }
    public void getRequests(Long id, Long idAccount, User user){
       int count = 0;
        for(Request request: savedRequestMap.values()){
            if(user.role.equals("tutor")&&request.getIdTutor().equals(idAccount)||user.role.equals("client")&&request.getIdClient().equals(idAccount)){
                count++;
            Long idUser = (user.role.equals("tutor"))?searchUserId(request.getIdTutor()):searchUserId(request.getIdClient());
            getRequestCard(id, idUser, request, user.role);}}
        if(count == 0 ){
            sendText(id, "Список заявок пуст", user.role);
            get_menu(id, user);
        }
    }
    public void getEditCriteria(Long id, User user){
        user.stage = "editCriteria";
        sendText_inline(id, editedCriteria.get(user.id_account).getSearchingCriteriaCard(), user.role, InlineButtons.setInline_editCriteria());
    }
    public void getStatistics(Long id, User user){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "Select * FROM get_info()";
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if(resultSet.next()){
                    String message = String.format("Общее число заявок на данный момент: %s\nЧисло заявок со статусом Не рассмотрено: %s\nЧисло заявок со статусом Одобрено: %s\nЧисло заявок со статусом Отклонено: %s", resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
                    sendText(id, message, user.role);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }


    public String tutor_card(Tutor tutor){
       return String.format("Репетитор\n%s %s\n%s", tutor.getSurname(), tutor.getName(), (tutor.getWork_experience() > 0)?("Опыт работы: " + tutor.getWork_experience()):"");
    }
    public String tutor_profile(Tutor tutor, boolean edited) {
        return String.format("%s\nРепетитор\nEmail: %s\nФамилия: %s\nИмя: %s\nВозраст: %s\nПол: %s\nГород: %s\nОпыт работы: %s\nОбразование: %s\nДополнительная информация: %s\nДисциплины: %s\n",(edited)?"Внесены несохраненные изменения":"", tutor.getEmail(), tutor.getSurname(), tutor.getName(),tutor.calculateAge(), tutor.getGender(), tutor.getCity(), ((tutor.getWork_experience() > 0) ? (tutor.getWork_experience()) : "[Не указано]"), ((tutor.getEducation().length() > 0) ? (tutor.getEducation()) : "[Не указано]"), ((tutor.getDopInfo().length() > 0) ? (tutor.getDopInfo()) : "[Не указано]"), tutor.getDisciplines_st());
    }
    public String client_profile(Client client, boolean edited) {
        return String.format("%s\nКлиент\nEmail: %s\nФамилия: %s\nИмя: %s\nСтатус: %s", ((edited)?"Внесены несохраненные изменения":""), client.getEmail(), client.getSurname(), client.getName(),client.getState());
    }

    public void deleteRequest(Long idRequest, String role){
        Request request = savedRequestMap.get(idRequest);
        Long idUser = (!role.equals("tutor"))?searchUserId(request.getIdTutor()):searchUserId(request.getIdClient());
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql;
            sql = "UPDATE public.request SET request_status=? WHERE idRequest = ?;";
            try {
                PreparedStatement statement1 = connection.prepareStatement(sql);
                if(role.equals("tutor")) {statement1.setString(1, "ОтклоненоT"); request.setRequest_status("ОтклоненоT");}
                else if(role.equals("client")) {statement1.setString(1, "ОтклоненоC"); request.setRequest_status("ОтклоненоC");}
                statement1.setLong(2, idRequest);
                statement1.executeUpdate();
                if(idUser != null){
                    sendText(idUser, "Заявка №" + request.getIdRequest() + " отклонена\n\n"+request.client_request(), role);
                }

            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }
        }
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}

    }
    public void get_tutors_in_map(User user){
        Search_criteria search_criteria = (user.role.equals("client"))?clientMap.get(user.id_account).getSearch_criteria():adminMap.get(user.id_account).getSearch_criteria();
        ArrayList<Tutor> tutors = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT * FROM get_tutor(?, ?, ?, ?, ?);";
            try{
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, search_criteria.getWork_experience());
                statement.setString(2, search_criteria.getGender());
                if(search_criteria.getAge() != null)
                    statement.setInt(3, search_criteria.getAge());
                else
                    statement.setNull(3, Types.INTEGER);
                statement.setString(4, search_criteria.getCity());
                Array array = connection.createArrayOf("VARCHAR", search_criteria.getDisciplines());
                statement.setArray(5, array);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    Tutor tutor = setTutor(resultSet, true);
                    tutorMap.putIfAbsent(tutor.getId_account(), tutor);
                    tutors.add(tutor);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
        user.tutors = tutors;
    }
    public Long check_auth(Long id){
        long id_account = 0;
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT account_idaccount FROM account_now where id_user = '" + id+"'";
            try{
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    if(resultSet.getString("account_idaccount") != null){
                        id_account = Long.parseLong(resultSet.getString("account_idaccount"));}
                }
                else {
                    sql = "INSERT INTO account_now(id_user) VALUES ('"+id+"');";
                    statement.executeUpdate(sql);}
                }
            catch (SQLException e) {
                e.printStackTrace();
            }}
            catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
        return id_account;}
    public long get_idAccount(){
        long id_account = 0L;
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try{
                Statement statement = connection.createStatement();
                String sql1 = "SELECT nextVal('account_idaccount_seq'::regclass) as id";
                ResultSet resultSet3 = statement.executeQuery(sql1);
                if (resultSet3.next()) {
                    id_account = Long.parseLong(resultSet3.getString("id"));}
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}

        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
        return id_account;
    }
    public void get_role(Long id_account, User user, String role){
        if(role == null ) role = "none";
        if(id_account != 0){
            if(role.equals("client")){
                getClient(id_account);
                user.id_account = id_account;
            }
            else {
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    String sql;Statement statement;ResultSet resultSet;
                    sql = "SELECT email, password_a, role FROM public.account Where idaccount = " + id_account;
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(sql);
                    if(resultSet.next()){
                        role = resultSet.getString("role");
                        user.id_account = id_account;
                        if(role.equals("client") && !clientMap.containsKey(user.id_account)){
                            sql = "SELECT * from client Where client.account_idaccount = " + id_account;
                            statement = connection.createStatement();
                            ResultSet resultSet2 = statement.executeQuery(sql);
                            if (resultSet2.next()) {
                                Client client = new Client();
                                client.setEmail(resultSet.getString("email"));
                                client.setPassword(MailData.decrypt(resultSet.getString("password_a")));
                                client.setId_account(id_account);
                                client.setSurname(resultSet2.getString("surname"));
                                client.setName(resultSet2.getString("name"));
                                client.setState(resultSet2.getString("state"));
                                sql = "SELECT age, gender, work_experience, city, disciplines FROM public.search_criteria where account_idaccount = " + client.getId_account();
                                statement.executeQuery(sql);
                                ResultSet resultSet3 = statement.executeQuery(sql);
                                if(resultSet.next()) {
                                    Search_criteria search_criteria = client.getSearch_criteria();
                                    if (resultSet3.getString(1)!= null)
                                        search_criteria.setAge(Integer.parseInt(resultSet3.getString(1)));
                                    search_criteria.setGender(resultSet3.getString(2));
                                    if (resultSet3.getString(3)!= null )
                                        search_criteria.setWork_experience(Integer.parseInt(resultSet3.getString(3)));
                                    search_criteria.setCity(resultSet3.getString(4));
                                    if(resultSet3.getString(5)!= null ){
                                        Array arrayDisciplines = resultSet.getArray(5);
                                        search_criteria.setDisciplines((String[]) arrayDisciplines.getArray());}}
                                clientMap.put(client.getId_account(), client);}}
                        else if(role.equals("tutor") && !tutorMap.containsKey(user.id_account)){
                            sql = "select * from tutor Where tutor.account_idaccount = " + id_account;
                            statement = connection.createStatement();
                            ResultSet resultSet2 = statement.executeQuery(sql);
                            if (resultSet2.next()) {
                                role = resultSet.getString("role");
                                Tutor tutor = setTutor(resultSet2, false);
                                tutor.setPassword(MailData.decrypt(resultSet.getString("password_a")));
                                tutor.setEmail(resultSet.getString("email"));
                                tutor.setId_account(id_account);
                                tutorMap.put(tutor.getId_account(), tutor);}
                        }
                        else if(role.equals("admin") && !adminMap.containsKey(user.id_account)){
                            Admin admin = new Admin();
                            admin.setEmail(resultSet.getString("email"));
                            admin.setPassword(MailData.decrypt(resultSet.getString("password_a")));
                            admin.setId_account(id_account);
                            sql = "SELECT age, gender, work_experience, city, disciplines FROM public.search_criteria where account_idaccount = " +admin.getId_account();
                            statement.executeQuery(sql);
                            resultSet = statement.executeQuery(sql);
                            if(resultSet.next()){
                                Search_criteria search_criteria = admin.getSearch_criteria();
                                if (resultSet.getString(1)!= null)
                                    search_criteria.setAge(Integer.parseInt(resultSet.getString(1)));
                                search_criteria.setGender(resultSet.getString(2));
                                if (resultSet.getString(3)!= null )
                                    search_criteria.setWork_experience(Integer.parseInt(resultSet.getString(3)));
                                search_criteria.setCity(resultSet.getString(4));
                                if(resultSet.getString(5)!= null ){
                                    Array arrayDisciplines = resultSet.getArray(5);
                                    search_criteria.setDisciplines((String[]) arrayDisciplines.getArray());}}
                            adminMap.put(admin.getId_account(), admin);
                        }
                    }}
                catch (SQLException e) {
                    System.out.println("Connection failed!");
                    e.printStackTrace();}}}
        user.role = role;
    }
    public void saveRequest(Long id, User user, Request request){
        requestMap.remove(user.id_account);
        String id_Tutor;
        long idRequest = 0L;
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql;
            Statement statement;
            try {
                try {
                    statement = connection.createStatement();
                    String sql1 = "SELECT nextVal('request_idrequest_seq'::regclass) as id";

                    ResultSet resultSet3 = statement.executeQuery(sql1);
                    if (resultSet3.next()) {
                        idRequest = Long.parseLong(resultSet3.getString("id"));
                        request.setIdRequest(idRequest);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Array array = connection.createArrayOf("TIMESTAMP", request.getLocalDateTimes().toArray());
                sql = "INSERT INTO public.request (idrequest, tutor_account_idaccount, client_account_idaccount, date_lesson, request_status, dop_info, date_request) VALUES (?, ?, ?, ?, ?, ?, ?);";
                PreparedStatement pstmt = connection.prepareStatement(sql);

                pstmt.setLong(1, idRequest);
                pstmt.setLong(2, request.getIdTutor());
                pstmt.setLong(3, request.getIdClient());
                pstmt.setArray(4, array);
                pstmt.setString(5, "Не рассмотрено");
                pstmt.setString(6, request.getDopInfo());
                pstmt.setObject(7, LocalDate.now());
                pstmt.executeUpdate();
                request.setRequest_status("Не рассмотрено");
                savedRequestMap.put(idRequest, request);
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }

            sql = "SELECT id_user FROM account_now where account_idaccount = " + request.getIdTutor() + "";
            try {
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    id_Tutor = resultSet.getString("id_user");
                    sendText_inline(Long.valueOf(id_Tutor), request.client_request(),user.role, InlineButtons.setInline_request_save(idRequest, id));
                    sendText(id, "Заявка сохранена и отправлена репетитору",user.role);
                } else {
                    sendText(id, "В данный момент репетитор не в сети, но он получит уведомление о заявке, как пройдет авторизацию.",user.role);
                }
                get_menu(id, user);
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }

        }
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }
    public void requestCancelOK(Long idRequest, User user, boolean ok){
        Long idClientUser;
        Long idTutorUser;
        Request request = savedRequestMap.get(idRequest);
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql;
            try {
                String sql1 = "SELECT tutor_account_idaccount, a2.id_user as tutor_id_user, client_account_idaccount, a1.id_user as client_id_user FROM public.request Left join account_now a1 ON client_account_idaccount=a1.account_idaccount Left join account_now a2 ON tutor_account_idaccount=a2.account_idaccount where idrequest = ?";
                PreparedStatement pstmt = connection.prepareStatement(sql1);
                pstmt.setLong(1, request.getIdRequest());
                ResultSet resultSet3 = pstmt.executeQuery();
                if (resultSet3.next()) {
                    idClientUser = Long.parseLong(resultSet3.getString("client_id_user"));
                    idTutorUser = Long.parseLong(resultSet3.getString("tutor_id_user"));
                    if(idClientUser != null){
                        if (!ok){
                            sendText(idClientUser, "Заявка №"+ request.getIdRequest()+" отклонена\n\n"+request.client_request() , user.role);}
                        else {
                            requestOkSend(idTutorUser, idClientUser, request, user.role);}}
                }
            }
            catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }

            sql = "UPDATE public.request SET request_status=? WHERE idRequest = ?;";
            try {
                PreparedStatement statement1 = connection.prepareStatement(sql);
                if(ok) {
                    statement1.setString(1, "Одобрено");
                    request.setRequest_status("Одобрено");
                }
                else {
                    statement1.setString(1, "ОтклоненоT");
                    request.setRequest_status("ОтклоненоT");
                }

                statement1.setLong(2, idRequest);
                statement1.executeUpdate();


            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }

        }
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }
    public void requestOkSend(Long idTutorUser, Long idClientUser, Request request, String role){
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = InlineButtons.setInline_deleteButton(request.getIdRequest());
        InlineKeyboardButton button3;
        buttons.add(buttons2);
        if(idTutorUser != null){
            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Связаться с репетитором");
            button3.setUrl("tg://user?id="+idTutorUser);
            buttons2.add(button3);
            buttons.add(buttons2);
        }
        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);

        sendText_inline(idClientUser, "Заявка №"+ request.getIdRequest()+" одобрена\n\n" + request.client_request(), role, markupKeyboard);
    }
    public void checkRequests(Long idAccount, String table, Long id){
        Long idUser;
        String table2 = table.equals("tutor")?"client":"tutor";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try {
                String sql1 = String.format("SELECT idRequest, %s_account_idaccount, a1.id_user as id_user, date_lesson, request_status, dop_info, date_request FROM public.request Left join account_now a1 ON %s_account_idaccount=a1.account_idaccount where %s_account_idaccount = %s AND date_request >= '%s'", table2, table2, table, idAccount, LocalDate.now().minusDays(7));
                Statement statement = connection.createStatement();
                ResultSet resultSet3 = statement.executeQuery(sql1);
                while (resultSet3.next()) {
                    Long client = (table.equals("client")) ? idAccount : Long.valueOf(resultSet3.getString("client_account_idaccount"));
                    Long tutor = (table.equals("tutor")) ? idAccount : Long.valueOf(resultSet3.getString("tutor_account_idaccount"));
                    if(!clientMap.containsKey(client)){
                        getClient(client);}
                    Client clientPr = clientMap.get(client);
                    Request request = new Request(client, tutor, clientPr);
                    request.setDopInfo(resultSet3.getString("dop_info"));
                    request.setRequest_status(resultSet3.getString("request_status"));
                    request.setIdRequest(Long.valueOf(resultSet3.getString("idRequest")));
                    request.setDate_request(LocalDate.parse(resultSet3.getString("date_request")));
                    Timestamp[] arrayDate = (Timestamp[]) resultSet3.getArray("date_lesson").getArray();
                    ArrayList<LocalDateTime> arrayList = new ArrayList<>();
                    for(Timestamp timestamp:arrayDate){
                        arrayList.add(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), ZoneId.systemDefault()));
                    }
                    request.setLocalDateTimes(arrayList);
                    savedRequestMap.putIfAbsent(Long.valueOf(resultSet3.getString("idRequest")), request);
                    idUser = (resultSet3.getString("id_user") == null)?null:Long.parseLong(resultSet3.getString("id_user"));
                    getRequestCard(id, idUser, request, table);
                }
            }

            catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
            }}
            catch (SQLException e) {
                System.out.println("Connection failed!");
                e.printStackTrace();}

    }
    public Tutor setTutor(ResultSet resultSet, boolean flag) throws SQLException {
        Tutor tutor = new Tutor();
        if(flag){
            tutor.setPassword(resultSet.getString("password_a"));
            tutor.setEmail(resultSet.getString("email"));
            tutor.setId_account(Long.parseLong(resultSet.getString("account_idaccount")));
        }
        tutor.setSurname(resultSet.getString("surname"));
        tutor.setName(resultSet.getString("name"));
        tutor.setGender(resultSet.getString("gender"));
        tutor.setCity(resultSet.getString("city"));
        tutor.setDate_of_birth(LocalDate.parse(resultSet.getString("date_of_birth")));
        tutor.setWork_experience(Integer.parseInt(resultSet.getString("work_experience")));
        tutor.setDopInfo(resultSet.getString("dop_info"));
        tutor.setEducation(resultSet.getString("education"));
        Array arrayDisciplines = resultSet.getArray("disciplines");
        tutor.setDisciplines((String[]) arrayDisciplines.getArray());
        return tutor;
    }
    public void getClient(Long id_account){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql;Statement statement;ResultSet resultSet;
            sql = "SELECT email, password_a, role FROM public.account Where idaccount = " + id_account;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                if(!clientMap.containsKey(id_account)){
                    sql = "SELECT * from client Where client.account_idaccount = " + id_account;
                    statement = connection.createStatement();
                    ResultSet resultSet2 = statement.executeQuery(sql);
                    if (resultSet2.next()) {
                        Client client = new Client();
                        client.setEmail(resultSet.getString("email"));
                        client.setPassword(MailData.decrypt(resultSet.getString("password_a")));
                        client.setId_account(id_account);
                        client.setSurname(resultSet2.getString("surname"));
                        client.setName(resultSet2.getString("name"));
                        client.setState(resultSet2.getString("state"));
                        sql = "SELECT age, gender, work_experience, city, disciplines FROM public.search_criteria where account_idaccount = " + client.getId_account();
                        statement.executeQuery(sql);
                        ResultSet resultSet3 = statement.executeQuery(sql);
                        if(resultSet.next()) {
                            Search_criteria search_criteria = client.getSearch_criteria();
                            if (resultSet3.getString(1)!= null)
                                search_criteria.setAge(Integer.parseInt(resultSet3.getString(1)));
                            search_criteria.setGender(resultSet3.getString(2));
                            if (resultSet3.getString(3)!= null )
                                search_criteria.setWork_experience(Integer.parseInt(resultSet3.getString(3)));
                            search_criteria.setCity(resultSet3.getString(4));
                            if(resultSet3.getString(5)!= null ){
                                Array arrayDisciplines = resultSet.getArray(5);
                                search_criteria.setDisciplines((String[]) arrayDisciplines.getArray());}}
                        clientMap.put(client.getId_account(), client);}}}}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }
    public void getRequestCard(Long id, Long idUser, Request request, String table){
        if (table.equals("client")) {
            switch (request.getRequest_status()) {
                case "Одобрено" -> requestOkSend(idUser, id, request, table);
                case "ОтклоненоT" -> sendText(id, "Заявка №" + request.getIdRequest() + " отклонена\n\n"+request.client_request(), table);
                case "Не рассмотрено" -> sendText_inline(id, "Заявка №" + request.getIdRequest() + " не рассмотрена\n\n"+request.client_request(), table,InlineButtons.setInline_deleteKeyboard(request.getIdRequest()));
            }
        } else {
            System.out.println("ok");
            switch (request.getRequest_status()) {
                case "Не рассмотрено" -> sendText_inline(id, "Заявка №" + request.getIdRequest() + " не рассмотрена:\n\n" + request.client_request(), table, InlineButtons.setInline_request_save(request.getIdRequest(), id));
                case "Одобрено" -> requestOkSend(idUser, id, request, table);
                case "ОтклоненоC" -> sendText(id, "Заявка №" + request.getIdRequest() + " отклонена\n\n" + request.client_request(), table);
            }
        }
    }
    public Long searchUserId(Long idAccount){
        for(Map.Entry<Long, User> user:userMap.entrySet()){
            if(user.getValue().id_account.equals(idAccount)){
                return user.getKey();}}
        return null;
    }
    public void saveCriteria(Long id, User user, Search_criteria search, int idMsg){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try {
                String sql1 = "UPDATE public.search_criteria SET age=?, gender=?, work_experience=?, city=?, disciplines=? WHERE account_idaccount=?;";
                PreparedStatement preparedStatement = connection.prepareStatement(sql1);
                preparedStatement.setString(2, search.getGender());
                preparedStatement.setInt(3, search.getWork_experience());
                preparedStatement.setString(4, search.getCity());
                Array array = connection.createArrayOf("VARCHAR", search.getDisciplines());
                preparedStatement.setArray(5, array);
                preparedStatement.setLong(6, user.id_account);
                if(search.getAge() != null){
                    preparedStatement.setInt(1, search.getAge());}
                else{
                    preparedStatement.setNull(1, Types.INTEGER);}
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
        if ((user.role.equals("client"))) {
            clientMap.get(user.id_account).setSearch_criteria(search);
        } else {
            adminMap.get(user.id_account).setSearch_criteria(search);
        }
        editMessage(id, "Критерии сохранены", idMsg);
        get_menu(id, user);

    }
    public void changePassword(String email, String pass){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = String.format("UPDATE public.account SET password_a='%s' WHERE email = '%s';", MailData.encrypt(pass), email);
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }
    public static boolean isValidEmailAddress(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    static class InlineButtons {
        private static InlineKeyboardMarkup setInline_reg1() {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Я ищю репетитора \uD83D\uDE4B \uD83D\uDE4B\u200D♂️");
            button1.setCallbackData("клиент_регистрация");
            buttons1.add(button1);
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setText("Я репетитор \uD83D\uDC69\u200D\uD83C\uDFEB \uD83D\uDC68\u200D\uD83C\uDFEB ");
            button2.setCallbackData("репетитор_регистрация");
            buttons1.add(button2);

            buttons.add(buttons1);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_reg2(User user) {
            if (user.dop_info.equals("клиент_регистрация")) {
                return setInline_status();
            } else if (user.dop_info.equals("репетитор_регистрация")) {
                return setInline_gender();
            }
            return null;

        }

        public static InlineKeyboardMarkup setInline_gender() {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons1;
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Мужской");
            button1.setCallbackData("Мужской");
            buttons1.add(button1);
            buttons.add(buttons1);
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setText("Женский");
            button2.setCallbackData("Женский");
            buttons1.add(button2);
            buttons.add(buttons1);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        public static InlineKeyboardMarkup setInline_status() {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons1;
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Школьник");
            button1.setCallbackData("Школьник");
            buttons1.add(button1);
            buttons.add(buttons1);
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setText("Студент");
            button2.setCallbackData("Студент");
            buttons1.add(button2);
            buttons.add(buttons1);
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("Работающий");
            button3.setCallbackData("Работающий");
            buttons1.add(button3);
            buttons.add(buttons1);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        public static InlineKeyboardMarkup setInline_password() {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons1;
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Назад");
            button1.setCallbackData("back_password");
            buttons1.add(button1);
            buttons.add(buttons1);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }
        public static InlineKeyboardMarkup setInline_checkPassword() {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons1;
            buttons1 = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("Забыли пароль?");
            button1.setCallbackData("check_password");
            buttons1.add(button1);
            buttons.add(buttons1);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_tutors(Tutor tutor, boolean previous, boolean next) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons1 = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("Открыть профиль");
            button3.setCallbackData("Открыть профиль-" + tutor.getId_account());
            buttons2.add(button3);
            if (previous) {
                InlineKeyboardButton button1 = new InlineKeyboardButton();
                button1.setText("Предыдущий");
                button1.setCallbackData("Предыдущий");
                buttons1.add(button1);
            }
            if (next) {
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                button2.setText("Следующий");
                button2.setCallbackData("Следующий");
                buttons1.add(button2);
            }
            buttons.add(buttons2);
            if (buttons1.size() != 0) {
                buttons.add(buttons1);
            }
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_tutor_profile(Tutor tutor) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("Оставить заявку");
            button3.setCallbackData("Оставить заявку_" + tutor.getId_account());
            buttons2.add(button3);
            buttons.add(buttons2);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_profile(String role) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2;
            InlineKeyboardButton button3;
            buttons2 = new ArrayList<>();

            button3 = new InlineKeyboardButton();
            button3.setText("Пароль");
            button3.setCallbackData("Пароль");
            buttons2.add(button3);

            button3 = new InlineKeyboardButton();
            button3.setText("Фамилия");
            button3.setCallbackData("Фамилия");
            buttons2.add(button3);

            button3 = new InlineKeyboardButton();
            button3.setText("Имя");
            button3.setCallbackData("Имя");
            buttons2.add(button3);

            if (role.equals("client")) {
                button3 = new InlineKeyboardButton();
                button3.setText("Статус");
                button3.setCallbackData("Статус");
                buttons2.add(button3);
                buttons.add(buttons2);
            } else if (role.equals("tutor")) {
                buttons.add(buttons2);
                buttons2 = new ArrayList<>();
                button3 = new InlineKeyboardButton();
                button3.setText("Дата рождения");
                button3.setCallbackData("Дата рождения");
                buttons2.add(button3);

                button3 = new InlineKeyboardButton();
                button3.setText("Пол");
                button3.setCallbackData("Пол");
                buttons2.add(button3);

                button3 = new InlineKeyboardButton();
                button3.setText("Опыт работы");
                button3.setCallbackData("Опыт работы");
                buttons2.add(button3);

                button3 = new InlineKeyboardButton();
                button3.setText("Город");
                button3.setCallbackData("Город");
                buttons2.add(button3);
                buttons.add(buttons2);

                buttons2 = new ArrayList<>();
                button3 = new InlineKeyboardButton();
                button3.setText("Образование");
                button3.setCallbackData("Образование");
                buttons2.add(button3);

                button3 = new InlineKeyboardButton();
                button3.setText("Дополнительная информация");
                button3.setCallbackData("Дополнительная информация");
                buttons2.add(button3);

                button3 = new InlineKeyboardButton();
                button3.setText("Дисциплины");
                button3.setCallbackData("Дисциплины");
                buttons2.add(button3);

                buttons.add(buttons2);
            }

            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Сохранить");
            button3.setCallbackData("Сохранить");
            buttons2.add(button3);
            buttons.add(buttons2);

            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_disciplines(String[] disciplines) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3;
            int count = 0;
            for (String discipline : disciplines) {
                count++;
                button3 = new InlineKeyboardButton();
                button3.setText(discipline);
                button3.setCallbackData("delete_" + discipline);
                buttons2.add(button3);
                if (count == 3) {
                    buttons.add(buttons2);
                    count = 0;
                    buttons2 = new ArrayList<>();
                }
            }
            if (count != 0) {
                buttons.add(buttons2);
                buttons2 = new ArrayList<>();
            }
            button3 = new InlineKeyboardButton();
            button3.setText("+");
            button3.setCallbackData("add_discipline");
            buttons2.add(button3);
            buttons.add(buttons2);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_new_disciplines(String[] disciplines) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3;
            int count = 0;
            for (String discipline : disciplines) {
                count++;
                button3 = new InlineKeyboardButton();
                button3.setText(discipline);
                button3.setCallbackData("new_" + discipline);
                buttons2.add(button3);
                if (count == 3) {
                    buttons.add(buttons2);
                    count = 0;
                    buttons2 = new ArrayList<>();
                }
            }
            if (count != 0) {
                buttons.add(buttons2);
            }
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_request_correct(String callback) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3;

            button3 = new InlineKeyboardButton();
            button3.setText("Удалить");
            button3.setCallbackData(callback + "_delete");
            buttons2.add(button3);
            buttons.add(buttons2);

            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Продолжить заполнять");
            button3.setCallbackData(callback + "_continue");
            buttons2.add(button3);

            buttons.add(buttons2);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_request(boolean date_choose, boolean time_choose) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3;


            if (!time_choose) {
                if (!date_choose) {
                    button3 = new InlineKeyboardButton();
                    button3.setText("Дополнительная информация");
                    button3.setCallbackData("Дополнительная информация");
                    buttons2.add(button3);
                    buttons.add(buttons2);
                    buttons2 = new ArrayList<>();

                    button3 = new InlineKeyboardButton();
                    button3.setText("Выбрать время и день недели");
                    button3.setCallbackData("Даты");
                    buttons2.add(button3);
                    buttons.add(buttons2);

                    buttons2 = new ArrayList<>();
                    button3 = new InlineKeyboardButton();
                    button3.setText("Отправить");
                    button3.setCallbackData("Отправить");
                    buttons2.add(button3);
                } else {
                    LocalDate date = LocalDate.now();
                    int lD = date.getDayOfWeek().getValue() + 1;
                    Locale usersLocale = Locale.getDefault();
                    DateFormatSymbols dfs = new DateFormatSymbols(usersLocale);
                    String[] weekdays = dfs.getWeekdays();
                    int count = 0;
                    for (int i = 1; i < 8; i++) {
                        count++;
                        if (count > 4) {
                            buttons.add(buttons2);
                            buttons2 = new ArrayList<>();
                            count = 1;
                        }
                        button3 = new InlineKeyboardButton();
                        button3.setText(weekdays[(i + lD < 8) ? (i + lD) : (i + lD) % 8 + 1]);
                        button3.setCallbackData("date_" + date.plusDays(i));
                        buttons2.add(button3);
                    }
                    buttons.add(buttons2);
                    buttons2 = new ArrayList<>();
                    button3 = new InlineKeyboardButton();
                    button3.setText("Очистить выбранные дни недели");
                    button3.setCallbackData("delete_date");
                    buttons2.add(button3);


                }
            } else {
                int count = 0;
                for (int i = 0; i < 25; i++) {
                    count++;
                    if (count > 5) {
                        count = 1;
                        buttons.add(buttons2);
                        buttons2 = new ArrayList<>();
                    }
                    button3 = new InlineKeyboardButton();
                    button3.setText(i + ":00");
                    button3.setCallbackData("time_" + ((i >= 10) ? i : "0" + i));
                    buttons2.add(button3);
                }
                if (count != 5) {
                    buttons.add(buttons2);
                }
                buttons2 = new ArrayList<>();

                button3 = new InlineKeyboardButton();
                button3.setText("Очистить выбранное время");
                button3.setCallbackData("delete_time");
                buttons2.add(button3);
                buttons.add(buttons2);

                buttons2 = new ArrayList<>();
                button3 = new InlineKeyboardButton();
                button3.setText("Вернуться к заявке");
                button3.setCallbackData("back");
                buttons2.add(button3);
            }
            buttons.add(buttons2);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static InlineKeyboardMarkup setInline_request_save(Long idRequest, Long id) {
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton> buttons2 = new ArrayList<>();
            InlineKeyboardButton button3;

            button3 = new InlineKeyboardButton();
            button3.setText("Отклонить");
            button3.setCallbackData("отклонить заявку_" + idRequest);
            buttons2.add(button3);

            button3 = new InlineKeyboardButton();
            button3.setText("Принять");
            button3.setCallbackData("принять заявку_" + idRequest);
            buttons2.add(button3);
            buttons.add(buttons2);

            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Связаться с клиентом");
            button3.setUrl("tg://user?id=" + id);
            buttons2.add(button3);
            buttons.add(buttons2);

            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }

        private static List<InlineKeyboardButton> setInline_deleteButton(Long idRequest){
            List<InlineKeyboardButton>buttons2 = new ArrayList<>();
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("Отклонить заявку");
            button3.setCallbackData("deleteRequest_"+idRequest);
            buttons2.add(button3);
            return buttons2;
        }
        private static InlineKeyboardMarkup setInline_deleteKeyboard(Long idRequest){
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            buttons.add(setInline_deleteButton(idRequest));
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }
        public static InlineKeyboardMarkup setInline_editCriteria(){
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton>buttons2 = new ArrayList<>();
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("Пол");
            button3.setCallbackData("criteria_gender");
            buttons2.add(button3);
            button3 = new InlineKeyboardButton();
            button3.setText("Возраст");
            button3.setCallbackData("criteria_age");
            buttons2.add(button3);
            buttons.add(buttons2);

            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Город");
            button3.setCallbackData("criteria_city");
            buttons2.add(button3);
            button3 = new InlineKeyboardButton();
            button3.setText("Опыт работы");
            button3.setCallbackData("criteria_workExperience");
            buttons2.add(button3);
            buttons.add(buttons2);
            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Дисциплины");
            button3.setCallbackData("criteria_discipline");
            buttons2.add(button3);
            buttons.add(buttons2);
            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Очистить критерии");
            button3.setCallbackData("criteria_clear");
            buttons2.add(button3);
            buttons.add(buttons2);
            buttons2 = new ArrayList<>();
            button3 = new InlineKeyboardButton();
            button3.setText("Сохранить критерии");
            button3.setCallbackData("criteria_save");
            buttons2.add(button3);
            buttons.add(buttons2);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }
        public static InlineKeyboardMarkup setInline_editCriteria2(String callback){
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            List<InlineKeyboardButton>buttons2 = new ArrayList<>();
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("Изменить");
            button3.setCallbackData("editCriteria_"+callback);
            buttons2.add(button3);
            button3 = new InlineKeyboardButton();
            button3.setText("Удалить");
            button3.setCallbackData("deleteCriteria_"+callback);
            buttons2.add(button3);
            buttons.add(buttons2);
            InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
            markupKeyboard.setKeyboard(buttons);
            return markupKeyboard;
        }


    }
    class RegistrationSystem{
        public void registration_0(Long id, User user){
            String message = """
                Добро пожаловать в нашу систему поиска репетиторов и клиентов репетиторам!
                Здесь вы сможете найти идеального репетитора для обучения любого предмета или найти клиентов для выгодного сотрудничества.\s
                Для полноценного использования всех возможностей нашей системы, необходимо зарегистрироваться.""";
            sendText(id, message, user.role);
            message = "Вы ищите репетитора или же вы репетитор? \n" +
                    "(Нажмите на одну из кнопок ниже)";
            sendText_inline(id, message, user.role, InlineButtons.setInline_reg1());
            user.stage = "registration";
            user.id_account = get_idAccount();
            user.step = 1;

        }

        public void registration_1(Long id, User user){
            String message = "*Введите email*";
            sendText(id, message, user.role);
            user.step = 2;
        }

        public void registration_2(Long id, User user, String email){
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                String sql = String.format("SELECT role FROM account where email =  '%s'", email);
                try{
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        String message = "Данный email уже используется. ";
                        sendText(id, message, user.role);
                        registration_1(id, user);
                    }
                    else {
                        if (isValidEmailAddress(email)){
                            if(user.dop_info.equals("клиент_регистрация")){
                                Client client = new Client();
                                client.setEmail(email);
                                client.setId_account(user.id_account);
                                clientMap.put(user.id_account, client);}

                            if(user.dop_info.equals("репетитор_регистрация")){
                                Tutor tutor = new Tutor();
                                tutor.setEmail(email);
                                tutor.setId_account(user.id_account);
                                tutorMap.put(user.id_account, tutor);}
                            user.step = 3;
                            String message = "*Введите пароль*\nУчитывайте, что в качестве пароля не стоит выбирать комбинации короче 8 символов.\nВ пароле желательно комбинировать цифры, заглавные и прописные буквы.";
                            sendText(id, message, user.role);
                        }
                        else {
                            String message = "Неверный email";
                            sendText(id, message, user.role);
                            registration_1(id, user);}
                    }}
                catch (SQLException e) {
                    e.printStackTrace();
                }}

            catch (SQLException e) {
                System.out.println("Connection failed!");
                e.printStackTrace();}
        }

        public void registration_3(Long id, User user, String password){
            if (user.dop_info.equals("репетитор_регистрация")){
                Tutor tutor = tutorMap.get(user.id_account);
                tutor.setPassword(password);
            }
            else if (user.dop_info.equals("клиент_регистрация")){
                Client client = clientMap.get(user.id_account);
                client.setPassword(password);}
            String message = "*Введите фамилию*";
            sendText(id, message, user.role);
            user.step = 4;
        }
        public void registration_4(Long id, User user, String surname){
            if (user.dop_info.equals("репетитор_регистрация")){
                Tutor tutor = tutorMap.get(user.id_account);
                tutor.setSurname(surname);
            }
            else if (user.dop_info.equals("клиент_регистрация")){
                Client client = clientMap.get(user.id_account);
                client.setSurname(surname);}
            String message = "*Введите имя*";
            sendText(id, message, user.role);
            user.step = 5;
        }
        public void registration_5(Long id, User user, String name){
            if (user.dop_info.equals("репетитор_регистрация")){
                Tutor tutor = tutorMap.get(user.id_account);
                tutor.setName(name);
                String message = "*Введите пол*\nВоспользуйтесь кнопками ниже";
                sendText_inline(id, message, user.role, InlineButtons.setInline_reg2(user));

            }
            else if (user.dop_info.equals("клиент_регистрация")){
                Client client = clientMap.get(user.id_account);
                client.setName(name);
                String message = "*Выберете род деятельности*\nВоспользуйтесь кнопками ниже";
                sendText_inline(id, message, user.role, InlineButtons.setInline_reg2(user));
            }
            user.step = 6;
        }

        public void registration_6(Long id, User user, String message){
            if (user.dop_info.equals("репетитор_регистрация")){
                Tutor tutor = tutorMap.get(user.id_account);
                tutor.setGender(message);
                String message_t = "*Введите город проживания*";
                sendText(id, message_t, user.role);
                user.step = 7;
            }
            else if (user.dop_info.equals("клиент_регистрация")){
                Client client = clientMap.get(user.id_account);
                client.setState(message);
                String message_t = "*Регистрация завершена*";
                sendText(id, message_t, user.role);
                client.insertAccountDataBase(url, username, password, "client");
                client.updateId_account(url, username, password, id, false);
                client.insertInfoDataBase(url, username, password);
                user.dop_info = null;
                user.role = "client";
                get_menu(id, user);
            }
        }

        public void registration_7(Long id, User user, String city){
            if (user.dop_info.equals("репетитор_регистрация")){
                Tutor tutor = tutorMap.get(user.id_account);
                tutor.setCity(city);
                String message = "*Введите дату рождения в формате - 01.01.2000*";
                sendText(id, message, user.role);
                user.step = 8;
            }
        }

        public void registration_8(Long id, User user, String date){
            if (user.dop_info.equals("репетитор_регистрация")){
                Tutor tutor = tutorMap.get(user.id_account);
                tutor.setDate_of_birth(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                tutor.insertAccountDataBase(url, username, password, "tutor");
                tutor.insertInfoDataBase(url, username, password);
                tutor.updateId_account(url, username, password, id, false);
                String message_t = "*Регистрация завершена*";
                sendText(id, message_t, user.role);
                user.dop_info = null;
                user.role = "tutor";
                get_menu(id, user);
            }
        }
    }
    class LoginSystem {
        public void login_0(Long id, User user) {
            String message = """
                    Добро пожаловать в нашу систему поиска репетиторов и клиентов репетиторам!
                    Здесь вы сможете найти идеального репетитора для обучения любого предмета или найти клиентов для выгодного сотрудничества.\s
                    Для полноценного использования всех возможностей нашей системы, необходимо войти.""";
            sendText(id, message, user.role);
            message = "*Введите email*";
            sendText(id, message, user.role);
            user.stage = "login";
            user.step = 1;

        }

        public void login_1(Long id, User user, String email) {
            user.dop_info = email;
            String message = "Введите пароль";
            if(mailActivation) {sendText_inline(id, message, user.role, InlineButtons.setInline_checkPassword());}
            else {sendText(id, message, user.role);}
            user.step = 2;
        }

        public void login_2(Long id, User user, String password_a) {

            try (Connection connection = DriverManager.getConnection(url, username, password)) {

                String sql = String.format("SELECT idaccount, role FROM account Where email = '%s' AND password_a = '%s'", user.dop_info, MailData.encrypt(password_a));
                try{
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        String message = "*Вход произведен.*";
                        Long id_account = Long.parseLong(resultSet.getString("idaccount"));
                        String role = resultSet.getString("role");
                        sql = "UPDATE account_now SET account_idaccount=NULL WHERE account_idaccount = " + id_account;
                        try{
                            statement = connection.createStatement();
                            statement.executeUpdate(sql);}
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                        get_role(id_account, user, role);
                        Account.updateId_account(url, username, password, id_account, id);
                        sendText(id, message, user.role);
                        if(!user.role.equals("admin")){
                            checkRequests(id_account, user.role, id);}
                        get_menu(id, user);
                    }
                    else {
                        String message = "Email или пароль введены неверно. Попробуйте снова или зарегистрируйтесь.";
                        sendText(id, message, user.role);
                        message = "*Введите email*";
                        sendText(id, message, user.role);
                        user.stage = "login";
                        user.dop_info = null;
                        user.step = 1;
                    }}
                catch (SQLException e) {
                    e.printStackTrace();
                }}

            catch (SQLException e) {
                System.out.println("Connection failed!");
                e.printStackTrace();}

        }
    }
    static class User implements Serializable {
        String stage = null;
        int step = 0;
        String dop_info;

        String role;
        Long id_account;

        ArrayList<Tutor> tutors;
        int place;

        public Tutor getTutorById(Long id){
            for (Tutor tutor:tutors) {
                if(tutor.getId_account().equals(id)) {return tutor;}}
            return null;
        }
    }
}


