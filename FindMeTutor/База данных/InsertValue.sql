INSERT INTO account(
	idaccount, email, password_a, role)
	VALUES (1, 'gmail.gmail1@gmail.com', 'Q0l4OZf1ArEV0LmxPQ6vQw==', 'admin'), (2, 'gmail.gmail2@gmail.com', 'Wc3ZSQe29PlC1UpLzRMtdw==', 'tutor'), (3, 'gmail.gmail3@gmail.com', 'zs1jrN9MhsE+KhsjHNsPFg==', 'tutor'), (4, 'gmail.gmail4@gmail.com', '0uBc6WuxQMk2GQGRi+iV6A==', 'tutor'), (5, 'gmail.gmail5@gmail.com', 'zyYoN2tr66PVjs27F3bGAA==', 'client');

INSERT INTO tutor(
	account_idaccount, surname, name, date_of_birth, gender, city, work_experience, disciplines, education, dop_info)
	VALUES (2, 'Иванова', 'Александра', '2000-09-12', 'Женский', 'Казань', 2, '{}', 'Среднее професиональное образование', 'Я хороший учитель'), 
(3, 'Петров', 'Александр', '1996-03-22', 'Мужской', 'Казань', 8, '{"история"}', 'Высшее', 'Я лучший учитель'),
 (4, 'Александров', 'Александр', '2004-07-27', 'Мужской', 'Москва', 8, '{"литература"}', 'Начальная школа', 'Я нормальный учитель, честно');

INSERT INTO client(
	account_idaccount, surname, name, state)
	VALUES (5, 'Весенняя', 'Герань', 'Студент');

INSERT INTO search_criteria(
	account_idaccount, work_experience)
	VALUES (1, 0), (5, 0);

INSERT INTO public.request(
	idrequest, tutor_account_idaccount, client_account_idaccount, date_request, dop_info, request_status, date_lesson)
	VALUES (1, 2, 5, '2023-05-19', 'А вы точно хороший преподователь?', 'Одобрено', '{}'), (2, 3, 5, '2023-05-19', 'Непонятные даты.. Как их учить?', 'Не рассмотрено', '{"2023-05-24 13:00:00", "2023-05-24 17:00:00"}'), (3, 4, 5, '2023-05-19', 'Я не умею читать больше 2 страниц. Научите.', 'ОтклоненоT', '{"2023-05-22 15:00:00"}');

