--
-- Data for Name: address; Type: TABLE DATA; Schema: public; Owner: postgres
--

--INSERT INTO public.address (id, created_at, updated_at, city, line1, line2, state) VALUES (1, '2019-08-13 12:40:58.449', '2019-08-13 12:40:58.449', 'Mumbai', 'Address Line 1', 'Address Line 2', 'Maharashtra');
--INSERT INTO public.address (id, created_at, updated_at, city, line1, line2, state) VALUES (2, '2019-08-13 15:25:55.122', '2019-08-13 15:25:55.122', 'Mumbai', 'Address Line 1', 'Address Line 2', 'Maharashtra');


--
-- Data for Name: admin; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.admin (id, created_at, updated_at, email, name, phone, title) VALUES (1, '2019-08-13 12:40:58.454', '2019-08-13 12:40:58.455', 'email@adminemail.com', 'Admin Name', '69713596342985', 'Admin Title');
INSERT INTO public.admin (id, created_at, updated_at, email, name, phone, title) VALUES (2, '2019-08-13 15:25:55.124', '2019-08-13 15:25:55.124', 'email@adminemail.com', 'Admin Name', '69713596342985', 'Admin Title');
INSERT INTO public.admin (id, created_at, updated_at, email, name, phone, title) VALUES (3, '2019-08-14 14:25:17.959', '2019-08-14 14:25:17.959', 'email@a.com', 'Admin Name', '12345687890', 'Admin Title');


--
-- Data for Name: organization; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.organization (id, created_at, updated_at, active, domain, health_system_name, license_key, name, admin_id, address_line1, address_line2, city, state) VALUES (1, '2019-08-13 12:40:58.457', '2019-08-13 12:40:58.457', true, 'www.sampledomainname.com', 'Health System Name 3', 'abcd', 'Health System Name 3', 1, NULL, NULL, NULL, NULL);
INSERT INTO public.organization (id, created_at, updated_at, active, domain, health_system_name, license_key, name, admin_id, address_line1, address_line2, city, state) VALUES (2, '2019-08-13 15:25:55.126', '2019-08-13 15:25:55.126', true, 'www.sampledomainname.com', 'hasdk', 'abcde', 'Health System Name 3', 2, NULL, NULL, NULL, NULL);
INSERT INTO public.organization (id, created_at, updated_at, active, domain, health_system_name, license_key, name, admin_id, address_line1, address_line2, city, state) VALUES (3, '2019-08-14 14:25:17.967', '2019-08-14 14:25:17.967', true, 'www.domain.com', 'Sample Health System name', '1234', NULL, 3, 'Address Line 1', NULL, 'Mumbai', 'Maharashtra');

--
-- Data for Name: affiliation; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.affiliation (id, created_at, updated_at, status, actioned_by, affiliation_from, affiliation_with, requested_by) VALUES (1, '2019-08-19 12:24:46.472', '2019-08-19 12:24:46.472', 'AFFILIATED', NULL, 1, 2, 2);


--
-- Name: admin_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.admin_sequence restart with (select max(id)+1 from public.admin);

--
-- Name: affiliation_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.affiliation_sequence restart with (select max(id)+1 from public.affiliation);


--
-- Name: organization_sequence; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.organization_sequence restart with (select max(id)+1 from public.organization);