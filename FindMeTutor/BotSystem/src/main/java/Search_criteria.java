import org.apache.commons.lang3.ArrayUtils;


public class Search_criteria implements Cloneable{
    private Integer age;
    private String gender;
    private String city;
    private int work_experience = 0;
    private String[] disciplines = new String[0];

    public String getSearchingCriteriaCard(){
        return String.format("Критерии поиска репетиторов\n%s\n%s\n%s\n%s\n%s", getValue("gender"), getValue("city"), getValue("age"), getValue("workExperience"),getValue("discipline"));
    }

    public void deleteCriteria(String criteria){
        if(criteria.equals("city")){
            city = null;
        }
        if(criteria.equals("workExperience")){
            work_experience = 0;
        }
        if(criteria.equals("gender")){
            gender = null;
        }
        if(criteria.equals("age")){
            age = null;
        }
        if(criteria.equals("discipline")){
            disciplines = null;
        }
    }

    public void setCriteria(String criteria, String text){
        if(criteria.equals("city")){
            city = text;}
        if(criteria.equals("workExperience")){
            work_experience = Integer.parseInt(text);}
        if(criteria.equals("gender")){
            gender = text;}
        if(criteria.equals("age")){
            age = Integer.valueOf(text);}
        if(criteria.equals("discipline")){
            if (!ArrayUtils.contains(disciplines, text)) {
                disciplines = ArrayUtils.add(disciplines, text);
            }
        }
    }
    public void setNull(){
        age =null;
        work_experience=0;
        gender=null;
        city = null;
        disciplines = new String[0];

    }

    public String getValue(String criteria){
        return switch (criteria) {
            case "gender" -> String.format("Пол: %s", (gender == null) ? "Не указано" : gender);
            case "age" -> String.format("Возраст не более: %s", (age == null) ? "Не указано" : age);
            case "workExperience" -> String.format("Опыт работы не менее: %s", work_experience);
            case "discipline" -> String.format("Дисциплины: %s", getDiscipline_st());
            case "city" -> String.format("Город проживания: %s", (city == null) ? "Не указано" : city);
            default -> null;
        };
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
    public String getDiscipline_st(){
        StringBuilder result = new StringBuilder();
        if(disciplines.length != 0){
            for(String discipline:disciplines){
                result.append(discipline).append(" ");}}
        else {
            result.append("Не указано");
        }
        return result.toString();
    }


    @Override
    public Search_criteria clone() {
        try {
            Search_criteria clone = (Search_criteria) super.clone();
            clone.disciplines = disciplines.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
