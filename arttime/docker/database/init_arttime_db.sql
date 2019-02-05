--
-- PostgreSQL database dump
--

-- Dumped from database version 10.4
-- Dumped by pg_dump version 10.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: arttime; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE arttime WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';

ALTER DATABASE arttime OWNER TO postgres;

\connect arttime

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: Day; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Day" (
    id bigint NOT NULL,
    comment character varying(255),
    date date NOT NULL,
    holiday boolean,
    shiftedfrom date,
    shiftedto date,
    working boolean,
    workdayscalendar_id bigint
);


ALTER TABLE public."Day" OWNER TO postgres;

--
-- Name: Day_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Day_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public."Day_id_seq" OWNER TO postgres;

--
-- Name: Day_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Day_id_seq" OWNED BY public."Day".id;


--
-- Name: employee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employee (
    username character varying(255) NOT NULL,
    department character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    firstname character varying(255) NOT NULL,
    former boolean NOT NULL,
    lastname character varying(255) NOT NULL,
    workload integer NOT NULL,
    calendar_id bigint
);


ALTER TABLE public.employee OWNER TO postgres;

--
-- Name: employee_accessibledepartments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employee_accessibledepartments (
    employee_username character varying(255) NOT NULL,
    accessibledepartments character varying(255)
);


ALTER TABLE public.employee_accessibledepartments OWNER TO postgres;

--
-- Name: filter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filter (
    id bigint NOT NULL,
    name character varying(255),
    owner character varying(255)
);


ALTER TABLE public.filter OWNER TO postgres;

--
-- Name: filter_departments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filter_departments (
    filter_id bigint NOT NULL,
    departments character varying(255)
);


ALTER TABLE public.filter_departments OWNER TO postgres;

--
-- Name: filter_employee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filter_employee (
    filter_id bigint NOT NULL,
    employees_username character varying(255) NOT NULL
);


ALTER TABLE public.filter_employee OWNER TO postgres;

--
-- Name: filter_hourtype; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filter_hourtype (
    filter_id bigint NOT NULL,
    hourtypes_id bigint NOT NULL
);


ALTER TABLE public.filter_hourtype OWNER TO postgres;

--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.filter_id_seq OWNER TO postgres;

--
-- Name: filter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.filter_id_seq OWNED BY public.filter.id;


--
-- Name: filter_project; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filter_project (
    filter_id bigint NOT NULL,
    projects_id bigint NOT NULL
);


ALTER TABLE public.filter_project OWNER TO postgres;

--
-- Name: hours; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hours (
    id bigint NOT NULL,
    approved boolean NOT NULL,
    comment character varying(255),
    date date NOT NULL,
    quantity numeric(19,2),
    employee_username character varying(255) NOT NULL,
    project_id bigint NOT NULL,
    type_id bigint NOT NULL,
    CONSTRAINT hours_quantity_check CHECK ((quantity >= (0)::numeric))
);


ALTER TABLE public.hours OWNER TO postgres;

--
-- Name: hours_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hours_id_seq OWNER TO postgres;

--
-- Name: hours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.hours_id_seq OWNED BY public.hours.id;


--
-- Name: hourtype; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hourtype (
    id bigint NOT NULL,
    actualtime boolean NOT NULL,
    priority integer NOT NULL,
    type character varying(255) NOT NULL
);


ALTER TABLE public.hourtype OWNER TO postgres;

--
-- Name: hourtype_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hourtype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hourtype_id_seq OWNER TO postgres;

--
-- Name: hourtype_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.hourtype_id_seq OWNED BY public.hourtype.id;


--
-- Name: project; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project (
    id bigint NOT NULL,
    allowemployeereporttime boolean NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(1000),
    masteridoremptystring character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    filtertype character varying(255),
    value character varying(255),
    master_id bigint
);


ALTER TABLE public.project OWNER TO postgres;

--
-- Name: project_employee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_employee (
    project_id bigint NOT NULL,
    team_username character varying(255) NOT NULL
);


ALTER TABLE public.project_employee OWNER TO postgres;

--
-- Name: project_hourtype; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_hourtype (
    project_id bigint NOT NULL,
    accountablehours_id bigint NOT NULL
);


ALTER TABLE public.project_hourtype OWNER TO postgres;

--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_id_seq OWNER TO postgres;

--
-- Name: project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.project_id_seq OWNED BY public.project.id;


--
-- Name: project_manager; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_manager (
    project_id bigint NOT NULL,
    manager_username character varying(255) NOT NULL
);


ALTER TABLE public.project_manager OWNER TO postgres;

--
-- Name: setting; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.setting (
    key character varying(255) NOT NULL,
    value character varying(255)
);


ALTER TABLE public.setting OWNER TO postgres;

--
-- Name: workdayscalendar; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.workdayscalendar (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.workdayscalendar OWNER TO postgres;

--
-- Name: workdayscalendar_departments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.workdayscalendar_departments (
    workdayscalendar_id bigint NOT NULL,
    departments character varying(255)
);


ALTER TABLE public.workdayscalendar_departments OWNER TO postgres;

--
-- Name: workdayscalendar_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.workdayscalendar_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.workdayscalendar_id_seq OWNER TO postgres;

--
-- Name: workdayscalendar_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.workdayscalendar_id_seq OWNED BY public.workdayscalendar.id;


--
-- Name: Day id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Day" ALTER COLUMN id SET DEFAULT nextval('public."Day_id_seq"'::regclass);


--
-- Name: filter id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter ALTER COLUMN id SET DEFAULT nextval('public.filter_id_seq'::regclass);


--
-- Name: hours id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hours ALTER COLUMN id SET DEFAULT nextval('public.hours_id_seq'::regclass);


--
-- Name: hourtype id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hourtype ALTER COLUMN id SET DEFAULT nextval('public.hourtype_id_seq'::regclass);


--
-- Name: project id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project ALTER COLUMN id SET DEFAULT nextval('public.project_id_seq'::regclass);


--
-- Name: workdayscalendar id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workdayscalendar ALTER COLUMN id SET DEFAULT nextval('public.workdayscalendar_id_seq'::regclass);


--
-- Data for Name: Day; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."Day" (id, comment, date, holiday, shiftedfrom, shiftedto, working, workdayscalendar_id) FROM stdin;
\.


--
-- Data for Name: employee; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.employee (username, department, email, firstname, former, lastname, workload, calendar_id) FROM stdin;
admin	Moscow	admin@email.com	Petr	f	Petrov	100	2
employee3	Moscow	kmilne@example.com	Keane	f	Milne	100	2
officemanager	Moscow	om@email.com	Pavel	f	Pavlov	100	2
projectmanager	Moscow	pm@email.com	Sergey	f	Sergeev	100	2
accountant	Prague	accountant@email.com	Elena	f	Petrova	100	1
employee4	Prague	kbenson@example.com	Ksawery	f	Benson	100	1
exec	Prague	exec@email.com	Vasily	f	Vasiliev	100	1
employee1	Warsaw	saidhopkins@example.com	Said	f	Hopkins	100	3
employee2	Warsaw	lnairn@example.com	Laurie	f	Nairn	100	3
officemanager2	Warsaw	kswith@example.com	Kyle	f	Swith	100	3
\.


--
-- Data for Name: employee_accessibledepartments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.employee_accessibledepartments (employee_username, accessibledepartments) FROM stdin;
officemanager	Moscow
officemanager2	Prague
officemanager2	Warsaw
accountant	Moscow
accountant	Prague
accountant	Warsaw
\.


--
-- Data for Name: filter; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filter (id, name, owner) FROM stdin;
\.


--
-- Data for Name: filter_departments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filter_departments (filter_id, departments) FROM stdin;
\.


--
-- Data for Name: filter_employee; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filter_employee (filter_id, employees_username) FROM stdin;
\.


--
-- Data for Name: filter_hourtype; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filter_hourtype (filter_id, hourtypes_id) FROM stdin;
\.


--
-- Data for Name: filter_project; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filter_project (filter_id, projects_id) FROM stdin;
\.


--
-- Data for Name: hours; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.hours (id, approved, comment, date, quantity, employee_username, project_id, type_id) FROM stdin;
\.


--
-- Data for Name: hourtype; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.hourtype (id, actualtime, priority, type) FROM stdin;
1	t	5	Regular time
2	f	0	Overtime
3	f	-2	Sick Leave
\.


--
-- Data for Name: project; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project (id, allowemployeereporttime, code, description, masteridoremptystring, status, filtertype, value, master_id) FROM stdin;
1	f	Developement	\N		ACTIVE	DISABLED	\N	\N
2	t	Mobile Application	\N	1	ACTIVE	DISABLED	\N	1
3	t	Web Site	\N	1	ACTIVE	BASED_ON_MASTER	\N	1
4	t	Management	\N		ACTIVE	DISABLED	\N	\N
5	t	Support	\N		ACTIVE	DISABLED	\N	\N
6	f	Days Off	\N		ACTIVE	DISABLED	\N	\N
9	f	Days Off Prague	\N	6	ACTIVE	DEPARTMENTS	Prague	6
8	f	Days Off Moscow	\N	6	ACTIVE	DEPARTMENTS	Moscow	6
7	f	Days Off Warsaw	\N	6	ACTIVE	DEPARTMENTS	Warsaw	6
10	t	Accounting	\N		ACTIVE	DISABLED	\N	\N
\.


--
-- Data for Name: project_employee; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_employee (project_id, team_username) FROM stdin;
2	employee4
2	employee3
2	employee2
3	employee2
4	projectmanager
4	officemanager
4	exec
5	admin
7	employee2
7	employee1
8	admin
8	projectmanager
8	employee3
8	officemanager
9	accountant
9	employee4
9	exec
10	accountant
7	officemanager2
\.


--
-- Data for Name: project_hourtype; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_hourtype (project_id, accountablehours_id) FROM stdin;
1	1
2	1
2	2
3	1
4	1
4	2
5	1
5	2
6	3
7	3
8	3
9	3
10	1
10	2
1	2
\.


--
-- Data for Name: project_manager; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_manager (project_id, manager_username) FROM stdin;
1	exec
3	exec
4	exec
5	exec
6	exec
7	exec
8	exec
9	exec
10	exec
2	projectmanager
\.


--
-- Data for Name: setting; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.setting (key, value) FROM stdin;
EMPLOYEE_TRACKING_SYSTEM_NAME	Keycloak
KEYCLOAK_REALM	master
KEYCLOAK_CACHE_REFRESH_INTERVAL_MINUTES	1
TEAM_TRACKING_SYSTEM_NAME	Keycloak
DEPARTMENT_TRACKING_SYSTEM_NAME	Keycloak
KEYCLOAK_USER_DEPARTMENT_ATTRIBUTE	department
TIMER_HOURS_INTERVAL	1
EMPLOYEE_SYNCHRONIZATION_ENABLED	true
HELP_PAGE_URL	https://github.com/Artezio/ART-TIME
APPLICATION_BASE_URL	http://localhost:8080/arttime
KEYCLOAK_SERVER_URL	http://keycloak:8081/auth
KEYCLOAK_CLIENT_ID	arttime
KEYCLOAK_USERNAME	admin
KEYCLOAK_PASSWORD	1
TEAM_SYNCHRONIZATION_ENABLED	true
\.


--
-- Data for Name: workdayscalendar; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.workdayscalendar (id, name) FROM stdin;
1	Czech
2	Russia
3	Poland
\.


--
-- Data for Name: workdayscalendar_departments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.workdayscalendar_departments (workdayscalendar_id, departments) FROM stdin;
2	Moscow
1	Prague
3	Warsaw
\.


--
-- Name: Day_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."Day_id_seq"', 1, false);


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.filter_id_seq', 1, false);


--
-- Name: hours_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.hours_id_seq', 1, false);


--
-- Name: hourtype_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.hourtype_id_seq', 3, true);


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_id_seq', 10, true);


--
-- Name: workdayscalendar_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.workdayscalendar_id_seq', 3, true);


--
-- Name: Day Day_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Day"
    ADD CONSTRAINT "Day_pkey" PRIMARY KEY (id);


--
-- Name: workdayscalendar constraint_unique_calendar_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workdayscalendar
    ADD CONSTRAINT constraint_unique_calendar_name UNIQUE (name);


--
-- Name: hours constraint_unique_hours; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hours
    ADD CONSTRAINT constraint_unique_hours UNIQUE (date, employee_username, project_id, type_id);


--
-- Name: hourtype constraint_unique_hourtype_name; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hourtype
    ADD CONSTRAINT constraint_unique_hourtype_name UNIQUE (type);


--
-- Name: employee employee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employee
    ADD CONSTRAINT employee_pkey PRIMARY KEY (username);


--
-- Name: filter_employee filter_employee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_employee
    ADD CONSTRAINT filter_employee_pkey PRIMARY KEY (filter_id, employees_username);


--
-- Name: filter_hourtype filter_hourtype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_hourtype
    ADD CONSTRAINT filter_hourtype_pkey PRIMARY KEY (filter_id, hourtypes_id);


--
-- Name: filter filter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter
    ADD CONSTRAINT filter_pkey PRIMARY KEY (id);


--
-- Name: filter_project filter_project_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_project
    ADD CONSTRAINT filter_project_pkey PRIMARY KEY (filter_id, projects_id);


--
-- Name: hours hours_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hours
    ADD CONSTRAINT hours_pkey PRIMARY KEY (id);


--
-- Name: hourtype hourtype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hourtype
    ADD CONSTRAINT hourtype_pkey PRIMARY KEY (id);


--
-- Name: project_employee project_employee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_employee
    ADD CONSTRAINT project_employee_pkey PRIMARY KEY (project_id, team_username);


--
-- Name: project_hourtype project_hourtype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_hourtype
    ADD CONSTRAINT project_hourtype_pkey PRIMARY KEY (project_id, accountablehours_id);


--
-- Name: project_manager project_manager_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_manager
    ADD CONSTRAINT project_manager_pkey PRIMARY KEY (project_id, manager_username);


--
-- Name: project project_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- Name: setting setting_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.setting
    ADD CONSTRAINT setting_pkey PRIMARY KEY (key);


--
-- Name: filter unique_filter_name_for_owner; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter
    ADD CONSTRAINT unique_filter_name_for_owner UNIQUE (owner, name);


--
-- Name: project uniqueprojectcodes; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project
    ADD CONSTRAINT uniqueprojectcodes UNIQUE (masteridoremptystring, code);


--
-- Name: workdayscalendar workdayscalendar_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workdayscalendar
    ADD CONSTRAINT workdayscalendar_pkey PRIMARY KEY (id);


--
-- Name: project_employee fk1vj0ww7sobvi366tm79db478y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_employee
    ADD CONSTRAINT fk1vj0ww7sobvi366tm79db478y FOREIGN KEY (project_id) REFERENCES public.project(id);


--
-- Name: hours fk25eg3tkqbs2cptn7f74amivdv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hours
    ADD CONSTRAINT fk25eg3tkqbs2cptn7f74amivdv FOREIGN KEY (employee_username) REFERENCES public.employee(username);


--
-- Name: filter_hourtype fk31sswkba5q453yk3pil7tflds; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_hourtype
    ADD CONSTRAINT fk31sswkba5q453yk3pil7tflds FOREIGN KEY (filter_id) REFERENCES public.filter(id);


--
-- Name: filter_departments fk6e6v9luqmq6tam4ipx9sub943; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_departments
    ADD CONSTRAINT fk6e6v9luqmq6tam4ipx9sub943 FOREIGN KEY (filter_id) REFERENCES public.filter(id);


--
-- Name: filter_employee fk8vydptfd49bwjcjeh6sbi3rp4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_employee
    ADD CONSTRAINT fk8vydptfd49bwjcjeh6sbi3rp4 FOREIGN KEY (filter_id) REFERENCES public.filter(id);


--
-- Name: filter_employee fk95lxd5jrr38omuf61hme8u2vf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_employee
    ADD CONSTRAINT fk95lxd5jrr38omuf61hme8u2vf FOREIGN KEY (employees_username) REFERENCES public.employee(username);


--
-- Name: hours fkb6rh8ni4e9gkwu6xfj97wwcne; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hours
    ADD CONSTRAINT fkb6rh8ni4e9gkwu6xfj97wwcne FOREIGN KEY (type_id) REFERENCES public.hourtype(id);


--
-- Name: project_manager fkba30fvdeg7lenk7glu04bx2dd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_manager
    ADD CONSTRAINT fkba30fvdeg7lenk7glu04bx2dd FOREIGN KEY (manager_username) REFERENCES public.employee(username);


--
-- Name: project_employee fkbbsyr0ap9hge71khw6sv0bxdm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_employee
    ADD CONSTRAINT fkbbsyr0ap9hge71khw6sv0bxdm FOREIGN KEY (team_username) REFERENCES public.employee(username);


--
-- Name: employee fkedr7ciye67qmikj8au8cw3xum; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employee
    ADD CONSTRAINT fkedr7ciye67qmikj8au8cw3xum FOREIGN KEY (calendar_id) REFERENCES public.workdayscalendar(id);


--
-- Name: hours fkf09x1q446ivutb73uti65rwym; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hours
    ADD CONSTRAINT fkf09x1q446ivutb73uti65rwym FOREIGN KEY (project_id) REFERENCES public.project(id);


--
-- Name: filter_project fkgqm4sakr2hmtxxnmwex0sd9qb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_project
    ADD CONSTRAINT fkgqm4sakr2hmtxxnmwex0sd9qb FOREIGN KEY (projects_id) REFERENCES public.project(id);


--
-- Name: Day fkgwxd43twh8vy3cxb7oxd5qvi8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Day"
    ADD CONSTRAINT fkgwxd43twh8vy3cxb7oxd5qvi8 FOREIGN KEY (workdayscalendar_id) REFERENCES public.workdayscalendar(id);


--
-- Name: employee_accessibledepartments fkhpmkjkik9qbqrj965xd66mj5t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employee_accessibledepartments
    ADD CONSTRAINT fkhpmkjkik9qbqrj965xd66mj5t FOREIGN KEY (employee_username) REFERENCES public.employee(username);


--
-- Name: filter_hourtype fkiqxtwuki6l2dl6edatyubplcv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_hourtype
    ADD CONSTRAINT fkiqxtwuki6l2dl6edatyubplcv FOREIGN KEY (hourtypes_id) REFERENCES public.hourtype(id);


--
-- Name: project fkm6v85x353b3grwag6561y8p31; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project
    ADD CONSTRAINT fkm6v85x353b3grwag6561y8p31 FOREIGN KEY (master_id) REFERENCES public.project(id);


--
-- Name: project_manager fkmh7dxmfk88jc2vncji4ri7jiv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_manager
    ADD CONSTRAINT fkmh7dxmfk88jc2vncji4ri7jiv FOREIGN KEY (project_id) REFERENCES public.project(id);


--
-- Name: project_hourtype fknma6y1h1c6eeno8gcgot6upae; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_hourtype
    ADD CONSTRAINT fknma6y1h1c6eeno8gcgot6upae FOREIGN KEY (project_id) REFERENCES public.project(id);


--
-- Name: workdayscalendar_departments fkp6uduyvp0j8viexncnp0811hr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workdayscalendar_departments
    ADD CONSTRAINT fkp6uduyvp0j8viexncnp0811hr FOREIGN KEY (workdayscalendar_id) REFERENCES public.workdayscalendar(id);


--
-- Name: project_hourtype fkpk1qom13dvgeph318efiy1d7c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_hourtype
    ADD CONSTRAINT fkpk1qom13dvgeph318efiy1d7c FOREIGN KEY (accountablehours_id) REFERENCES public.hourtype(id);


--
-- Name: filter_project fkqdj18s1u6eg5g606vgt4ybod2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter_project
    ADD CONSTRAINT fkqdj18s1u6eg5g606vgt4ybod2 FOREIGN KEY (filter_id) REFERENCES public.filter(id);


--
-- PostgreSQL database dump complete
--

