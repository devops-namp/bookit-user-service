-- PASSWORD: MojaSifra123

INSERT INTO bookit_users (username, password, email, role, firstname, lastname, city)
VALUES ('username1', 'y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=', 'name@example.com', 'GUEST', 'Ime', 'Prezime', 'Novi Sad');

INSERT INTO bookit_users (username, password, email, role, firstname, lastname, city)
VALUES ('username2', 'y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=', 'name2@example.com', 'HOST', 'Ime', 'Prezime', 'Novi Sad');

INSERT INTO registration_info (city, code, email, firstname, lastname, password, role, username)
VALUES ('Novi Sad', '123ABC', 'request@test.com', 'Name', 'Surname', 'y4t6TyLTziBF7p9CT75tfqTGmMiNMAs5dyzMZKL2e9g=', 'HOST', 'some_user');