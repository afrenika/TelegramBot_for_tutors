import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Tutor extends Account{
        private String surname;
        private String name;
        private LocalDate date_of_birth;
        private String gender;
        private String city;
        private int work_experience = 0;
        private String[] disciplines = new String[0];
        private String education = "";
        private String dopInfo = "";




        public String getSurname() {
                return surname;
        }

        public void setSurname(String surname) {
                this.surname = surname;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }


        public LocalDate getDate_of_birth() {
                return date_of_birth;
        }

        public void setDate_of_birth(LocalDate date_of_birth) {
                this.date_of_birth = date_of_birth;
        }

        public String getGender() {
                return gender;
        }

        public void setGender(String gender) {
                this.gender = gender;
        }

        public String getCity() {
                return city;
        }

        public void setCity(String city) {
                this.city = city;
        }

        public int getWork_experience() {
                return work_experience;
        }

        public void setWork_experience(int work_experience) {
                this.work_experience = work_experience;
        }

        public String[] getDisciplines() {
                return disciplines;
        }

        public void setDisciplines(String[] disciplines) {
                this.disciplines = disciplines;
        }

        public String getEducation() {
                return education;
        }

        public void setEducation(String education) {
                this.education = education;
        }

        public String getDopInfo() {
                return dopInfo;
        }

        public void setDopInfo(String dopInfo) {
                this.dopInfo = dopInfo;
        }

        @Override
        public void insertInfoDataBase(String url, String username, String password) {
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                        String sql = String.format("INSERT INTO public.tutor(account_idaccount, surname, name, date_of_birth, gender, city) VALUES (%s, '%s', '%s', '%s', '%s', '%s');", getId_account(), surname, name, date_of_birth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), gender, city);
                        try{
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

        public int calculateAge() {
                if (date_of_birth != null) {
                        return Period.between(date_of_birth, LocalDate.now()).getYears();
                } else {
                        return 0;}
        }

        public String getDisciplines_st(){
                StringBuilder result = new StringBuilder();
                for(String discipline:disciplines){
                        if(result.length() != 0) {
                                result.append(", ");}
                        result.append(discipline);}
                return result.toString();}


        @Override
        public Account clone() {
                Tutor clone = (Tutor) super.clone();
                clone.setDisciplines(disciplines.clone());
                // TODO: copy mutable state here, so the clone can't change the internals of the original
                return clone;
        }

        public String getChanges_c(Tutor account2) {
                String result = "";
                if(!surname.equals(account2.surname)){
                        result += "surname = '" + account2.surname + "',";
                }
                if(!name.equals(account2.name)){
                        result += "name = '" + account2.name + "',";
                }
                if(!city.equals(account2.city)){
                        result += "city = '" + account2.city + "',";
                }
                if(!dopInfo.equals(account2.dopInfo)){
                        result += "dop_info = '" + account2.dopInfo + "',";
                }
                if(work_experience != (account2.work_experience)){
                        result += "work_experience = " + account2.work_experience + ",";
                }
                if(!education.equals(account2.education)){
                        result += "education = '" + account2.education + "',";
                }
                if(!date_of_birth.equals(account2.date_of_birth)){
                        result += "date_of_birth = '" + account2.date_of_birth + "',";
                }
                if(!gender.equals(account2.gender)){
                        result += "gender = '" + account2.gender + "',";
                }
                if(!Arrays.equals(disciplines, account2.disciplines)){
                        StringBuilder d = new StringBuilder("'{\"");
                        for(String discipline:account2.disciplines){
                                d.append(discipline).append("\", \"");
                        }
                        result += "disciplines = " + d + "}',";

                }
                result = result.substring(0, result.length() - 1);
                return result;
        }

        }



