
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Request {
    private Long idRequest;
    private Client client;
    private Long idClient;
    private Long idTutor;
    private List<LocalDateTime> localDateTimes;
    private String dopInfo = "";
    private String request_status;
    private LocalDate date_request;




    public String client_request(){
        return String.format("Заявка на обучение\nEmail: %s\nФамилия: %s\nИмя: %s\nСтатус: %s\nДаты: %s\nДополнительная информация: %s", client.getEmail(), client.getSurname(), client.getName(),client.getState(), getLocalDateTimes_st(), dopInfo);
    }

    public Request(Long idClient, Long idTutor, Client client) {
        this.idClient = idClient;
        this.idTutor = idTutor;
        this.localDateTimes = new ArrayList<>();
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getLocalDateTimes_st(){
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        Collections.sort(localDateTimes);
        int dayOfWeek = -1;
        StringBuilder result = new StringBuilder();
        for(LocalDateTime localDateTime:localDateTimes){
            if(dayOfWeek != (localDateTime.getDayOfWeek().getValue())){
                result.append("\n");
                result.append(dfs.getWeekdays()[localDateTime.getDayOfWeek().getValue()+1]).append("|  ");
                dayOfWeek = localDateTime.getDayOfWeek().getValue();}
            result.append(localDateTime.toLocalTime()).append("  ");}
        return result.toString();
}
    public String getLocalDateTimes_st(int day_of_week){
        Locale usersLocale = Locale.getDefault();
        DateFormatSymbols dfs = new DateFormatSymbols(usersLocale);
        List<LocalDateTime> day_of_week_sorted = localDateTimes.stream().filter(x->x.getDayOfWeek().getValue()==day_of_week).toList();
        StringBuilder result = new StringBuilder(dfs.getWeekdays()[day_of_week+1] + "|  ");
        for(LocalDateTime localDateTime:day_of_week_sorted){
            result.append(localDateTime.toLocalTime()).append("  ");
            }
        return result.toString();
    }

    public Long getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(Long idRequest) {
        this.idRequest = idRequest;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status = request_status;
    }

    public void delete_time(LocalDate date){
        localDateTimes.removeIf(x -> x.toLocalDate().equals(date));
    }

    public void delete_date(){
        localDateTimes.clear();
    }



    public Long getIdClient() {
        return idClient;
    }

    public void setIdClient(Long idClient) {
        this.idClient = idClient;
    }

    public Long getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(Long idTutor) {
        this.idTutor = idTutor;
    }

    public List<LocalDateTime> getLocalDateTimes() {
        return localDateTimes;
    }

    public void setLocalDateTimes(List<LocalDateTime> localDateTimes) {
        this.localDateTimes = localDateTimes;
    }

    public String getDopInfo() {
        return dopInfo;
    }

    public void setDopInfo(String dopInfo) {
        this.dopInfo = dopInfo;
    }

    public LocalDate getDate_request() {
        return date_request;
    }

    public void setDate_request(LocalDate date_request) {
        this.date_request = date_request;
    }
}
