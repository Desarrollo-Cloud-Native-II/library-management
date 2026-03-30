SET DEFINE OFF;
SET VERIFY OFF;
SET FEEDBACK ON;
SET SERVEROUTPUT ON;

CREATE SEQUENCE seq_user_id
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_book_id
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_loan_id
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE TABLE users (
    id              VARCHAR2(50)    NOT NULL,
    first_name      VARCHAR2(100)   NOT NULL,
    last_name       VARCHAR2(100)   NOT NULL,
    rut             VARCHAR2(20)    NOT NULL,
    email           VARCHAR2(200)   NOT NULL,
    active          NUMBER(1)       DEFAULT 1 NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_rut UNIQUE (rut),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_active CHECK (active IN (0, 1))
);

CREATE TABLE books (
    id                  VARCHAR2(50)    NOT NULL,
    title               VARCHAR2(300)   NOT NULL,
    author              VARCHAR2(200)   NOT NULL,
    publisher           VARCHAR2(200),
    publication_year    NUMBER(4),
    language            VARCHAR2(50),
    isbn                VARCHAR2(20),
    genre               VARCHAR2(100),
    description         VARCHAR2(2000),
    status              VARCHAR2(20)    DEFAULT 'AVAILABLE' NOT NULL,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_books PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_status CHECK (status IN ('AVAILABLE', 'BORROWED', 'RESERVED')),
    CONSTRAINT ck_books_year CHECK (publication_year >= 1000 AND publication_year <= 9999)
);

CREATE TABLE loans (
    id                      VARCHAR2(50)    NOT NULL,
    book_id                 VARCHAR2(50)    NOT NULL,
    user_id                 VARCHAR2(50)    NOT NULL,
    loan_date               DATE            NOT NULL,
    expected_return_date    DATE            NOT NULL,
    actual_return_date      DATE,
    status                  VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL,
    created_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_loans PRIMARY KEY (id),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_loans_status CHECK (status IN ('ACTIVE', 'RETURNED', 'OVERDUE')),
    CONSTRAINT ck_loans_dates CHECK (expected_return_date >= loan_date)
);

CREATE INDEX idx_users_rut ON users(rut);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);

CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_status ON books(status);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_title ON books(title);

CREATE INDEX idx_loans_book_id ON loans(book_id);
CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_loan_date ON loans(loan_date);
CREATE INDEX idx_loans_expected_return ON loans(expected_return_date);

INSERT INTO users (id, first_name, last_name, rut, email, active) VALUES ('1', 'Juan', 'Pérez', '12345678-9', 'juan.perez@email.com', 1);
INSERT INTO users (id, first_name, last_name, rut, email, active) VALUES ('2', 'María', 'González', '23456789-0', 'maria.gonzalez@email.com', 1);
INSERT INTO users (id, first_name, last_name, rut, email, active) VALUES ('3', 'Pedro', 'Martínez', '34567890-1', 'pedro.martinez@email.com', 1);
INSERT INTO users (id, first_name, last_name, rut, email, active) VALUES ('4', 'Ana', 'López', '45678901-2', 'ana.lopez@email.com', 0);
INSERT INTO users (id, first_name, last_name, rut, email, active) VALUES ('5', 'Carlos', 'Rodríguez', '56789012-3', 'carlos.rodriguez@email.com', 1);

INSERT INTO books (id, title, author, publisher, publication_year, language, isbn, genre, description, status) VALUES ('1', 'Cien Años de Soledad', 'Gabriel García Márquez', 'Editorial Sudamericana', 1967, 'Spanish', '978-0307474728', 'Realismo Mágico', 'Obra maestra del realismo mágico', 'AVAILABLE');
INSERT INTO books (id, title, author, publisher, publication_year, language, isbn, genre, description, status) VALUES ('2', '1984', 'George Orwell', 'Secker & Warburg', 1949, 'English', '978-0451524935', 'Distopía', 'Novela distópica sobre el totalitarismo', 'AVAILABLE');
INSERT INTO books (id, title, author, publisher, publication_year, language, isbn, genre, description, status) VALUES ('3', 'Don Quijote de la Mancha', 'Miguel de Cervantes', 'Francisco de Robles', 1605, 'Spanish', '978-8424936464', 'Novela', 'La obra más destacada de la literatura española', 'AVAILABLE');
INSERT INTO books (id, title, author, publisher, publication_year, language, isbn, genre, description, status) VALUES ('4', 'El Principito', 'Antoine de Saint-Exupéry', 'Reynal & Hitchcock', 1943, 'French', '978-0156012195', 'Fábula', 'Novela corta sobre un pequeño príncipe', 'BORROWED');
INSERT INTO books (id, title, author, publisher, publication_year, language, isbn, genre, description, status) VALUES ('5', 'Harry Potter y la Piedra Filosofal', 'J.K. Rowling', 'Bloomsbury', 1997, 'English', '978-0439708180', 'Fantasía', 'Primera entrega de la saga Harry Potter', 'AVAILABLE');

INSERT INTO loans (id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status) VALUES ('1', '4', '1', TO_DATE('2026-03-15', 'YYYY-MM-DD'), TO_DATE('2026-03-29', 'YYYY-MM-DD'), NULL, 'ACTIVE');
INSERT INTO loans (id, book_id, user_id, loan_date, expected_return_date, actual_return_date, status) VALUES ('2', '2', '2', TO_DATE('2026-03-01', 'YYYY-MM-DD'), TO_DATE('2026-03-15', 'YYYY-MM-DD'), TO_DATE('2026-03-14', 'YYYY-MM-DD'), 'RETURNED');

COMMIT;


