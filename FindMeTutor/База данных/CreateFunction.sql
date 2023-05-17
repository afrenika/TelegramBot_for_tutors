CREATE OR REPLACE FUNCTION public.get_info(
	)
    RETURNS TABLE(count1 bigint, count2 bigint, count3 bigint, count4 bigint) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE  
xSQL VARCHAR(1000);

BEGIN
xSQL := 'SELECT Count(idRequest) as request_count, (Select Count(idRequest) FROM request where request_status = ''Не рассмотрено'') as not_checked_count_request, (Select Count(idRequest) FROM request where request_status = ''Одобрено'') as ok_count_request, (Select Count(idRequest) FROM request where request_status Like ''Отклонено%'') as delete_count_request FROM request';
   return query execute xSQL;
END; 
$BODY$;


CREATE OR REPLACE FUNCTION public.get_tutor(
	work_experience1 integer,
	gender1 character varying,
	age1 integer,
	city1 character varying,
	disciplines1 character varying[])
    RETURNS TABLE(account_idaccount bigint, surname character varying, name character varying, date_of_birth date, gender character varying, city character varying, work_experience integer, disciplines character varying[], education character varying, dop_info text, idaccount bigint, email character varying, password_a character varying, role character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE  
xSQL VARCHAR(1000);
x varchar;

BEGIN
  
xSQL := 'select * from tutor inner join account On account_idaccount = idaccount where ';
  if not(gender1 is null) then
    xSQL = xSQL || 'gender = ''' || gender1 || ''' AND ';
  end if;
  if not(age1 is null) then
    xSQL = xSQL || 'date_part(''year'',age(date_of_birth)) <= ' || age1 || ' AND ';
  end if;
  if not(city1 is null) then
    xSQL = xSQL || 'city = ''' || city1 || ''' AND ';
  end if;
  if not(disciplines1 is null OR array_length(disciplines1, 1) = 0) Then 
  xSQL = xSQL || '(';
	  FOREACH x IN ARRAY disciplines1
	  LOOP
		xSQL = xSQL || ''''||x||''' = ANY(disciplines) OR';
	  END LOOP;
	  xSQL = substring(xSQL FROM 1 FOR length(xSQL)-3) || ') AND ';
  end if;
  xSQL = xSQL || 'work_experience >= '|| work_experience1|| ' group by account_idaccount, surname, name, date_of_birth, gender, work_experience, disciplines , education, dop_info, city, idaccount, email, password_a, role Limit 100;';
  raise notice '%',xSQL;
    return query execute xSQL;
END; 
$BODY$;