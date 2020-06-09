#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Grammar 
        add column grammarBibliographicalReference varchar(255);

-- schema update # 2

    alter table Grammar 
        add column grammarCopyrightInfo longtext;

-- schema update # 3

    alter table Grammar 
        add column grammarEditorialInfo longtext;

-- schema update # 4

    alter table Grammar 
        add column grammarISBNInfo varchar(255);

-- schema update # 5

    alter table Grammar 
        add column grammarOtherSignHubSeries longtext;

-- schema update # 6

    alter table Grammar 
        add column grammarSignHubSeries varchar(255);

-- schema update # 7

    alter table Grammar 
        add column grammarSignHubSeriesNumber integer;
