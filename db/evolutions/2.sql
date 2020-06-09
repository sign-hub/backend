#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    create table Media (
        uuid varchar(255) not null,
        createdAt datetime,
        deleted boolean,
        mediaName varchar(255),
        mediaPath varchar(255),
        mediaType varchar(255),
        mediaAuthor_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 2

    create table Metadata (
        uuid varchar(255) not null,
        keyfield varchar(255),
        valuefield varchar(255),
        report_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 3

    create table Option (
        uuid varchar(255) not null,
        keyfield varchar(255),
        valuefield varchar(255),
        question_uuid varchar(255),
        slide_uuid varchar(255),
        slideContentComponent_uuid varchar(255),
        test_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 4

    create table Question (
        uuid varchar(255) not null,
        deleted boolean,
        transitionType varchar(255),
        test_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 5

    create table Question_Option (
        Question_uuid varchar(255) not null,
        options_uuid varchar(255) not null
    );

-- schema update # 6

    create table Question_Slide (
        Question_uuid varchar(255) not null,
        slides_uuid varchar(255) not null
    );

-- schema update # 7

    create table Report (
        uuid varchar(255) not null,
        effectiveElapsedDate datetime,
        endedAt datetime,
        reportCsvPath varchar(255),
        reportDate datetime,
        startedAt datetime,
        author_uuid varchar(255),
        reportTest_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 8

    create table ReportQuestion (
        uuid varchar(255) not null,
        effectiveElapsedDate datetime,
        endedAt datetime,
        orderfield integer,
        startedAt datetime,
        question_uuid varchar(255),
        report_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 9

    create table ReportQuestion_ReportSlide (
        ReportQuestion_uuid varchar(255) not null,
        slides_uuid varchar(255) not null
    );

-- schema update # 10

    create table ReportSlide (
        uuid varchar(255) not null,
        answers varchar(255),
        effectiveElapsedDate datetime,
        endedAt datetime,
        expectedAnswers varchar(255),
        mouseTracking varchar(255),
        presentationOrder integer,
        startedAt datetime,
        transitionPerformed varchar(255),
        slide_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 11

    create table Report_Metadata (
        Report_uuid varchar(255) not null,
        metadata_uuid varchar(255) not null
    );

-- schema update # 12

    create table Report_ReportQuestion (
        Report_uuid varchar(255) not null,
        questions_uuid varchar(255) not null
    );

-- schema update # 13

    create table Slide (
        uuid varchar(255) not null,
        deleted boolean,
        transitionType varchar(255),
        typefield varchar(255),
        question_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 14

    create table SlideContentComponent (
        uuid varchar(255) not null,
        componentType varchar(255),
        dim varchar(255),
        pos varchar(255),
        media_uuid varchar(255),
        slide_uuid varchar(255),
        test_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 15

    create table SlideContentComponent_Option (
        SlideContentComponent_uuid varchar(255) not null,
        options_uuid varchar(255) not null
    );

-- schema update # 16

    create table Slide_Option (
        Slide_uuid varchar(255) not null,
        options_uuid varchar(255) not null
    );

-- schema update # 17

    create table Slide_SlideContentComponent (
        Slide_uuid varchar(255) not null,
        slideContent_uuid varchar(255) not null
    );

-- schema update # 18

    create table Test (
        uuid varchar(255) not null,
        deleted boolean,
        revisionDate datetime,
        state varchar(255),
        testName varchar(255),
        author_uuid varchar(255),
        primary key (uuid)
    );

-- schema update # 19

    create table Test_Option (
        Test_uuid varchar(255) not null,
        options_uuid varchar(255) not null
    );

-- schema update # 20

    create table Test_Question (
        Test_uuid varchar(255) not null,
        questions_uuid varchar(255) not null
    );

-- schema update # 21

    alter table Question_Option 
        add constraint UK_rk30kg68hro883bswk1vow8ge unique (options_uuid);

-- schema update # 22

    alter table Question_Slide 
        add constraint UK_lvc45uhe2u8pk2datsj6spxal unique (slides_uuid);

-- schema update # 23

    alter table ReportQuestion_ReportSlide 
        add constraint UK_6vit8l31bf71pxkrqx55i45ho unique (slides_uuid);

-- schema update # 24

    alter table Report_Metadata 
        add constraint UK_qwtfxvxm8wa01gyrw9pudgi43 unique (metadata_uuid);

-- schema update # 25

    alter table Report_ReportQuestion 
        add constraint UK_64stjwlcx03qovakycbuvg89w unique (questions_uuid);

-- schema update # 26

    alter table SlideContentComponent_Option 
        add constraint UK_kuigwq8i5lvkk075tr7n94irg unique (options_uuid);

-- schema update # 27

    alter table Slide_Option 
        add constraint UK_mt6k1fdgck43eg8mx7qp7tdtt unique (options_uuid);

-- schema update # 28

    alter table Slide_SlideContentComponent 
        add constraint UK_p5jn104pkt739b8b8t2q3bk74 unique (slideContent_uuid);

-- schema update # 29

    alter table Test_Option 
        add constraint UK_8eqmfddle57gop4qqpc0u6uaj unique (options_uuid);

-- schema update # 30

    alter table Test_Question 
        add constraint UK_19nhhwlnqmob3q4afd7lus6uo unique (questions_uuid);

-- schema update # 31

    alter table Media 
        add index FK_2jokm9639webc71m663g2g6yk (mediaAuthor_uuid), 
        add constraint FK_2jokm9639webc71m663g2g6yk 
        foreign key (mediaAuthor_uuid) 
        references EndUser (uuid);

-- schema update # 32

    alter table Metadata 
        add index FK_39ytmxe02nll8xhrxtk1mbea (report_uuid), 
        add constraint FK_39ytmxe02nll8xhrxtk1mbea 
        foreign key (report_uuid) 
        references Report (uuid);

-- schema update # 33

    alter table Option 
        add index FK_eawn5ebi5n0vb27xkf57gfsgm (question_uuid), 
        add constraint FK_eawn5ebi5n0vb27xkf57gfsgm 
        foreign key (question_uuid) 
        references Question (uuid);

-- schema update # 34

    alter table Option 
        add index FK_755nuyolqk3krvm7b3qsnake0 (slide_uuid), 
        add constraint FK_755nuyolqk3krvm7b3qsnake0 
        foreign key (slide_uuid) 
        references Slide (uuid);

-- schema update # 35

    alter table Option 
        add index FK_eoqjunnadgpsx7dr5ilb7bw2c (slideContentComponent_uuid), 
        add constraint FK_eoqjunnadgpsx7dr5ilb7bw2c 
        foreign key (slideContentComponent_uuid) 
        references SlideContentComponent (uuid);

-- schema update # 36

    alter table Option 
        add index FK_cw0qldqn191stsk7o4nbxnxql (test_uuid), 
        add constraint FK_cw0qldqn191stsk7o4nbxnxql 
        foreign key (test_uuid) 
        references Test (uuid);

-- schema update # 37

    alter table Question 
        add index FK_3u5be6nyduwxj4uk3g8xlmtfn (test_uuid), 
        add constraint FK_3u5be6nyduwxj4uk3g8xlmtfn 
        foreign key (test_uuid) 
        references Test (uuid);

-- schema update # 38

    alter table Question_Option 
        add index FK_rk30kg68hro883bswk1vow8ge (options_uuid), 
        add constraint FK_rk30kg68hro883bswk1vow8ge 
        foreign key (options_uuid) 
        references Option (uuid);

-- schema update # 39

    alter table Question_Option 
        add index FK_mxnmcp6vfkswhgatc05vbp523 (Question_uuid), 
        add constraint FK_mxnmcp6vfkswhgatc05vbp523 
        foreign key (Question_uuid) 
        references Question (uuid);

-- schema update # 40

    alter table Question_Slide 
        add index FK_lvc45uhe2u8pk2datsj6spxal (slides_uuid), 
        add constraint FK_lvc45uhe2u8pk2datsj6spxal 
        foreign key (slides_uuid) 
        references Slide (uuid);

-- schema update # 41

    alter table Question_Slide 
        add index FK_nxyobrly5dv5391suwsy3the4 (Question_uuid), 
        add constraint FK_nxyobrly5dv5391suwsy3the4 
        foreign key (Question_uuid) 
        references Question (uuid);

-- schema update # 42

    alter table Report 
        add index FK_xmlvtskn093fv864dm8n7ntu (author_uuid), 
        add constraint FK_xmlvtskn093fv864dm8n7ntu 
        foreign key (author_uuid) 
        references EndUser (uuid);

-- schema update # 43

    alter table Report 
        add index FK_666qw50slxk47ble2wfkj44qs (reportTest_uuid), 
        add constraint FK_666qw50slxk47ble2wfkj44qs 
        foreign key (reportTest_uuid) 
        references Test (uuid);

-- schema update # 44

    alter table ReportQuestion 
        add index FK_p4uf10d9qxy7pb6br3yxsli8e (question_uuid), 
        add constraint FK_p4uf10d9qxy7pb6br3yxsli8e 
        foreign key (question_uuid) 
        references Question (uuid);

-- schema update # 45

    alter table ReportQuestion 
        add index FK_fqcuglih9edr6ydtwi5s203me (report_uuid), 
        add constraint FK_fqcuglih9edr6ydtwi5s203me 
        foreign key (report_uuid) 
        references Report (uuid);

-- schema update # 46

    alter table ReportQuestion_ReportSlide 
        add index FK_6vit8l31bf71pxkrqx55i45ho (slides_uuid), 
        add constraint FK_6vit8l31bf71pxkrqx55i45ho 
        foreign key (slides_uuid) 
        references ReportSlide (uuid);

-- schema update # 47

    alter table ReportQuestion_ReportSlide 
        add index FK_219elw4g8b9ud4nmmqeom7nyx (ReportQuestion_uuid), 
        add constraint FK_219elw4g8b9ud4nmmqeom7nyx 
        foreign key (ReportQuestion_uuid) 
        references ReportQuestion (uuid);

-- schema update # 48

    alter table ReportSlide 
        add index FK_mhlrf7obb2ygu0nule0h2c7on (slide_uuid), 
        add constraint FK_mhlrf7obb2ygu0nule0h2c7on 
        foreign key (slide_uuid) 
        references Slide (uuid);

-- schema update # 49

    alter table Report_Metadata 
        add index FK_qwtfxvxm8wa01gyrw9pudgi43 (metadata_uuid), 
        add constraint FK_qwtfxvxm8wa01gyrw9pudgi43 
        foreign key (metadata_uuid) 
        references Metadata (uuid);

-- schema update # 50

    alter table Report_Metadata 
        add index FK_rhd69xdm779s03rsfrtn59ue3 (Report_uuid), 
        add constraint FK_rhd69xdm779s03rsfrtn59ue3 
        foreign key (Report_uuid) 
        references Report (uuid);

-- schema update # 51

    alter table Report_ReportQuestion 
        add index FK_64stjwlcx03qovakycbuvg89w (questions_uuid), 
        add constraint FK_64stjwlcx03qovakycbuvg89w 
        foreign key (questions_uuid) 
        references ReportQuestion (uuid);

-- schema update # 52

    alter table Report_ReportQuestion 
        add index FK_pgey1y1rw1lawyekgomsoqxvc (Report_uuid), 
        add constraint FK_pgey1y1rw1lawyekgomsoqxvc 
        foreign key (Report_uuid) 
        references Report (uuid);

-- schema update # 53

    alter table Slide 
        add index FK_i8i7st3jgwctkfeo7qkq1n4br (question_uuid), 
        add constraint FK_i8i7st3jgwctkfeo7qkq1n4br 
        foreign key (question_uuid) 
        references Question (uuid);

-- schema update # 54

    alter table SlideContentComponent 
        add index FK_jpdmortgujprf3vrjm1mvtse1 (media_uuid), 
        add constraint FK_jpdmortgujprf3vrjm1mvtse1 
        foreign key (media_uuid) 
        references Media (uuid);

-- schema update # 55

    alter table SlideContentComponent 
        add index FK_pf0xbd1wdjsjk8b6h53vh9gqc (slide_uuid), 
        add constraint FK_pf0xbd1wdjsjk8b6h53vh9gqc 
        foreign key (slide_uuid) 
        references Slide (uuid);

-- schema update # 56

    alter table SlideContentComponent 
        add index FK_6asvxybvo7sq5t1503uq5y7yh (test_uuid), 
        add constraint FK_6asvxybvo7sq5t1503uq5y7yh 
        foreign key (test_uuid) 
        references Test (uuid);

-- schema update # 57

    alter table SlideContentComponent_Option 
        add index FK_kuigwq8i5lvkk075tr7n94irg (options_uuid), 
        add constraint FK_kuigwq8i5lvkk075tr7n94irg 
        foreign key (options_uuid) 
        references Option (uuid);

-- schema update # 58

    alter table SlideContentComponent_Option 
        add index FK_drvjbilr4tp4t9ylcqlkjxaes (SlideContentComponent_uuid), 
        add constraint FK_drvjbilr4tp4t9ylcqlkjxaes 
        foreign key (SlideContentComponent_uuid) 
        references SlideContentComponent (uuid);

-- schema update # 59

    alter table Slide_Option 
        add index FK_mt6k1fdgck43eg8mx7qp7tdtt (options_uuid), 
        add constraint FK_mt6k1fdgck43eg8mx7qp7tdtt 
        foreign key (options_uuid) 
        references Option (uuid);

-- schema update # 60

    alter table Slide_Option 
        add index FK_spvg3nau4pcoxas48tuvhue5u (Slide_uuid), 
        add constraint FK_spvg3nau4pcoxas48tuvhue5u 
        foreign key (Slide_uuid) 
        references Slide (uuid);

-- schema update # 61

    alter table Slide_SlideContentComponent 
        add index FK_p5jn104pkt739b8b8t2q3bk74 (slideContent_uuid), 
        add constraint FK_p5jn104pkt739b8b8t2q3bk74 
        foreign key (slideContent_uuid) 
        references SlideContentComponent (uuid);

-- schema update # 62

    alter table Slide_SlideContentComponent 
        add index FK_71p4mqadoa445bp7a5pw0swi (Slide_uuid), 
        add constraint FK_71p4mqadoa445bp7a5pw0swi 
        foreign key (Slide_uuid) 
        references Slide (uuid);

-- schema update # 63

    alter table Test 
        add index FK_o9m5ymaslwy7ax0w4v9mkv01b (author_uuid), 
        add constraint FK_o9m5ymaslwy7ax0w4v9mkv01b 
        foreign key (author_uuid) 
        references EndUser (uuid);

-- schema update # 64

    alter table Test_Option 
        add index FK_8eqmfddle57gop4qqpc0u6uaj (options_uuid), 
        add constraint FK_8eqmfddle57gop4qqpc0u6uaj 
        foreign key (options_uuid) 
        references Option (uuid);

-- schema update # 65

    alter table Test_Option 
        add index FK_g9nkf30vyle6c9wcljck7x6gf (Test_uuid), 
        add constraint FK_g9nkf30vyle6c9wcljck7x6gf 
        foreign key (Test_uuid) 
        references Test (uuid);

-- schema update # 66

    alter table Test_Question 
        add index FK_19nhhwlnqmob3q4afd7lus6uo (questions_uuid), 
        add constraint FK_19nhhwlnqmob3q4afd7lus6uo 
        foreign key (questions_uuid) 
        references Question (uuid);

-- schema update # 67

    alter table Test_Question 
        add index FK_p8coqxoo6sxi73p29i9j0w97s (Test_uuid), 
        add constraint FK_p8coqxoo6sxi73p29i9j0w97s 
        foreign key (Test_uuid) 
        references Test (uuid);

# --- !Downs