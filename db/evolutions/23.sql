#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Contacts 
        add column updateDate datetime;

-- schema update # 2

    alter table Contacts 
        add column updatedBy_uuid varchar(255);

-- schema update # 3

    alter table EndUser 
        add column updateDate datetime;

-- schema update # 4

    alter table EndUser 
        add column updatedBy_uuid varchar(255);

-- schema update # 5

    alter table Feature 
        add column updateDate datetime;

-- schema update # 6

    alter table Feature 
        add column updatedBy_uuid varchar(255);

-- schema update # 7

    alter table FeatureArea 
        add column updateDate datetime;

-- schema update # 8

    alter table FeatureArea 
        add column updatedBy_uuid varchar(255);

-- schema update # 9

    alter table FeatureValue 
        add column updateDate datetime;

-- schema update # 10

    alter table FeatureValue 
        add column updatedBy_uuid varchar(255);

-- schema update # 11

    alter table Grammar 
        add column updateDate datetime;

-- schema update # 12

    alter table Grammar 
        add column updatedBy_uuid varchar(255);

-- schema update # 13

    alter table GrammarPart 
        add column updateDate datetime;

-- schema update # 14

    alter table GrammarPart 
        add column updatedBy_uuid varchar(255);

-- schema update # 15

    alter table Media 
        add column updateDate datetime;

-- schema update # 16

    alter table Media 
        add column updatedBy_uuid varchar(255);

-- schema update # 17

    alter table Metadata 
        add column updateDate datetime;

-- schema update # 18

    alter table Metadata 
        add column updatedBy_uuid varchar(255);

-- schema update # 19

    alter table Option 
        add column updateDate datetime;

-- schema update # 20

    alter table Option 
        add column updatedBy_uuid varchar(255);

-- schema update # 21

    alter table Question 
        add column updateDate datetime;

-- schema update # 22

    alter table Question 
        add column updatedBy_uuid varchar(255);

-- schema update # 23

    alter table Report 
        add column updateDate datetime;

-- schema update # 24

    alter table Report 
        add column updatedBy_uuid varchar(255);

-- schema update # 25

    alter table ReportQuestion 
        add column updateDate datetime;

-- schema update # 26

    alter table ReportQuestion 
        add column updatedBy_uuid varchar(255);

-- schema update # 27

    alter table ReportSlide 
        add column updateDate datetime;

-- schema update # 28

    alter table ReportSlide 
        add column updatedBy_uuid varchar(255);

-- schema update # 29

    alter table SignLanguage 
        add column updateDate datetime;

-- schema update # 30

    alter table SignLanguage 
        add column updatedBy_uuid varchar(255);

-- schema update # 31

    alter table Slide 
        add column updateDate datetime;

-- schema update # 32

    alter table Slide 
        add column updatedBy_uuid varchar(255);

-- schema update # 33

    alter table SlideContentComponent 
        add column updateDate datetime;

-- schema update # 34

    alter table SlideContentComponent 
        add column updatedBy_uuid varchar(255);

-- schema update # 35

    alter table Test 
        add column updateDate datetime;

-- schema update # 36

    alter table Test 
        add column updatedBy_uuid varchar(255);

-- schema update # 37

    alter table Topic 
        add column updateDate datetime;

-- schema update # 38

    alter table Topic 
        add column updatedBy_uuid varchar(255);

-- schema update # 39

    alter table Contacts 
        add index FK_o0ii71wexpstfrduwj3tp350j (updatedBy_uuid), 
        add constraint FK_o0ii71wexpstfrduwj3tp350j 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 40

    alter table EndUser 
        add index FK_6es7adqm8in9btnt5nmggd71j (updatedBy_uuid), 
        add constraint FK_6es7adqm8in9btnt5nmggd71j 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 41

    alter table Feature 
        add index FK_cm1i6eflsw4v6j1napbudvjxh (updatedBy_uuid), 
        add constraint FK_cm1i6eflsw4v6j1napbudvjxh 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 42

    alter table FeatureArea 
        add index FK_mvav5or4wfdlvr1qkao6lc52f (updatedBy_uuid), 
        add constraint FK_mvav5or4wfdlvr1qkao6lc52f 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 43

    alter table FeatureValue 
        add index FK_b25r7vlsjceues629exey0iw (updatedBy_uuid), 
        add constraint FK_b25r7vlsjceues629exey0iw 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 44

    alter table Grammar 
        add index FK_8k2blliovvx7kjmc8352jeevy (updatedBy_uuid), 
        add constraint FK_8k2blliovvx7kjmc8352jeevy 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 45

    alter table GrammarPart 
        add index FK_q1xmumy7dcvylirpeetgpl2nc (updatedBy_uuid), 
        add constraint FK_q1xmumy7dcvylirpeetgpl2nc 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 46

    alter table Media 
        add index FK_njk5f0kix5tc9jq3332ea50dm (updatedBy_uuid), 
        add constraint FK_njk5f0kix5tc9jq3332ea50dm 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 47

    alter table Metadata 
        add index FK_rsfnip87c8rm0tn1aqt99ddkk (updatedBy_uuid), 
        add constraint FK_rsfnip87c8rm0tn1aqt99ddkk 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 48

    alter table Option 
        add index FK_h6exlcq8smdn2850belj7lhxu (updatedBy_uuid), 
        add constraint FK_h6exlcq8smdn2850belj7lhxu 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 49

    alter table Question 
        add index FK_49tcs768tnn8meikbsmrlnwqe (updatedBy_uuid), 
        add constraint FK_49tcs768tnn8meikbsmrlnwqe 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 50

    alter table Report 
        add index FK_ixsa13dascmyncurqxjcd3p4h (updatedBy_uuid), 
        add constraint FK_ixsa13dascmyncurqxjcd3p4h 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 51

    alter table ReportQuestion 
        add index FK_7q8310nnqx1udph0u071t7xqn (updatedBy_uuid), 
        add constraint FK_7q8310nnqx1udph0u071t7xqn 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 52

    alter table ReportSlide 
        add index FK_on6qow5urri7bt6ar6e0od3ig (updatedBy_uuid), 
        add constraint FK_on6qow5urri7bt6ar6e0od3ig 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 53

    alter table SignLanguage 
        add index FK_9imep6amdpkfmed3b5538eyia (updatedBy_uuid), 
        add constraint FK_9imep6amdpkfmed3b5538eyia 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 54

    alter table Slide 
        add index FK_pnd92pw4cgtl1wn2uw4o1eix6 (updatedBy_uuid), 
        add constraint FK_pnd92pw4cgtl1wn2uw4o1eix6 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 55

    alter table SlideContentComponent 
        add index FK_j0au1ft1710c5nqyvy7y1c546 (updatedBy_uuid), 
        add constraint FK_j0au1ft1710c5nqyvy7y1c546 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 56

    alter table Test 
        add index FK_byola2uml3of2rvdv6l8yki15 (updatedBy_uuid), 
        add constraint FK_byola2uml3of2rvdv6l8yki15 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);

-- schema update # 57

    alter table Topic 
        add index FK_10n1ifhojt54ml2ow9mw318g3 (updatedBy_uuid), 
        add constraint FK_10n1ifhojt54ml2ow9mw318g3 
        foreign key (updatedBy_uuid) 
        references EndUser (uuid);