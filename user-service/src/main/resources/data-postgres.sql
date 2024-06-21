-- PASSWORD: MojaSifra123

INSERT INTO public."notification-settings" (reservationrequestcreated, reservationdeclined, personalreview, accommodationreview, reservationrequestresolved)
VALUES (true, true, true, true, true);

INSERT INTO public."notification-settings" (reservationrequestcreated, reservationdeclined, personalreview, accommodationreview, reservationrequestresolved)
VALUES (false, true, false, true, false);

INSERT INTO public."notification-settings" (reservationrequestcreated, reservationdeclined, personalreview, accommodationreview, reservationrequestresolved)
VALUES (true, false, true, false, true);

INSERT INTO public."bookit-users" (autoapprove, rejectedReservationscount, username, password, email, role, firstname, lastname, city, notification_settings_id)
VALUES (true, 1, 'username1', 'y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=', 'name@example.com', 'GUEST', 'Ime', 'Prezime', 'Novi Sad', 1);

INSERT INTO public."bookit-users" (autoapprove, rejectedReservationscount, username, password, email, role, firstname, lastname, city, notification_settings_id)
VALUES (false, 2, 'username2', 'y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=', 'name2@example.com', 'HOST', 'Ime', 'Prezime', 'Novi Sad', 2);

INSERT INTO public."bookit-users" (autoapprove, rejectedReservationscount, username, password, email, role, firstname, lastname, city, notification_settings_id)
VALUES (true, 1, 'username3', 'y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=', 'name3@example.com', 'HOST', 'Ime2', 'Prezime2', 'Beograd', 3);
