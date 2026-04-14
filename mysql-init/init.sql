-- we don't know how to generate root <with-no-name> (class Root) :(

grant select on performance_schema.* to 'mysql.session'@localhost;

grant trigger on sys.* to 'mysql.sys'@localhost;

grant audit_abort_exempt, firewall_exempt, select, system_user on *.* to 'mysql.infoschema'@localhost;

grant audit_abort_exempt, authentication_policy_admin, backup_admin, clone_admin, connection_admin, firewall_exempt, persist_ro_variables_admin, session_variables_admin, shutdown, super, system_user, system_variables_admin on *.* to 'mysql.session'@localhost;

grant audit_abort_exempt, firewall_exempt, system_user on *.* to 'mysql.sys'@localhost;

grant allow_nonexistent_definer, alter, alter routine, application_password_admin, audit_abort_exempt, audit_admin, authentication_policy_admin, backup_admin, binlog_admin, binlog_encryption_admin, clone_admin, connection_admin, create, create role, create routine, create tablespace, create temporary tables, create user, create view, delete, drop, drop role, encryption_key_admin, event, execute, file, firewall_exempt, flush_optimizer_costs, flush_privileges, flush_status, flush_tables, flush_user_resources, group_replication_admin, group_replication_stream, index, innodb_redo_log_archive, innodb_redo_log_enable, insert, lock tables, optimize_local_table, passwordless_user_admin, persist_ro_variables_admin, process, references, reload, replication client, replication slave, replication_applier, replication_slave_admin, resource_group_admin, resource_group_user, role_admin, select, sensitive_variables_observer, service_connection_admin, session_variables_admin, set_any_definer, show databases, show view, show_routine, shutdown, super, system_user, system_variables_admin, table_encryption_admin, telemetry_log_admin, transaction_gtid_tag, trigger, update, xa_recover_admin, grant option on *.* to root;

grant allow_nonexistent_definer, alter, alter routine, application_password_admin, audit_abort_exempt, audit_admin, authentication_policy_admin, backup_admin, binlog_admin, binlog_encryption_admin, clone_admin, connection_admin, create, create role, create routine, create tablespace, create temporary tables, create user, create view, delete, drop, drop role, encryption_key_admin, event, execute, file, firewall_exempt, flush_optimizer_costs, flush_privileges, flush_status, flush_tables, flush_user_resources, group_replication_admin, group_replication_stream, index, innodb_redo_log_archive, innodb_redo_log_enable, insert, lock tables, optimize_local_table, passwordless_user_admin, persist_ro_variables_admin, process, references, reload, replication client, replication slave, replication_applier, replication_slave_admin, resource_group_admin, resource_group_user, role_admin, select, sensitive_variables_observer, service_connection_admin, session_variables_admin, set_any_definer, show databases, show view, show_routine, shutdown, super, system_user, system_variables_admin, table_encryption_admin, telemetry_log_admin, transaction_gtid_tag, trigger, update, xa_recover_admin, grant option on *.* to root@localhost;

create table prediction_db.users
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)            null,
    email      varchar(255)           not null,
    password   varchar(255)           not null,
    role       enum ('ADMIN', 'USER') not null,
    username   varchar(255)           not null,
    constraint UK6dotkott2kjsp8vw4d0m25fb7
        unique (email),
    constraint UKr43af9ap4edm43mmtq01oddj6
        unique (username)
);

create table prediction_db.leagues
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)             null,
    description varchar(255)            null,
    external_id varchar(255)            null,
    name        varchar(255)            not null,
    sport       varchar(255)            null,
    type        enum ('CUSTOM', 'REAL') not null,
    created_by  bigint                  null,
    constraint FKipmt89js3vgro5k171tmfqvkm
        foreign key (created_by) references prediction_db.users (id)
);

create table prediction_db.leaderboard
(
    id                bigint auto_increment
        primary key,
    predictions_count int    null,
    total_points      int    null,
    league_id         bigint null,
    user_id           bigint null,
    constraint FKkrvli8v2u3owoa54i6hc2l0bu
        foreign key (user_id) references prediction_db.users (id),
    constraint FKmsmjnwlcpicuasudde9ndiht7
        foreign key (league_id) references prediction_db.leagues (id)
);

create table prediction_db.teams
(
    id          bigint auto_increment
        primary key,
    external_id varchar(255) null,
    logo_url    varchar(255) null,
    name        varchar(255) not null,
    league_id   bigint       null,
    constraint FKcmnrlwu7alyse9s3x5tgvxyqj
        foreign key (league_id) references prediction_db.leagues (id)
);

create table prediction_db.matches
(
    id           bigint auto_increment
        primary key,
    away_score   int                                   null,
    created_at   datetime(6)                           null,
    external_id  varchar(255)                          null,
    first_scorer varchar(255)                          null,
    home_score   int                                   null,
    mvp_player   varchar(255)                          null,
    start_time   datetime(6)                           null,
    status       enum ('FINISHED', 'LIVE', 'UPCOMING') null,
    away_team_id bigint                                null,
    home_team_id bigint                                null,
    league_id    bigint                                null,
    constraint FK23dnop04r2pfj2wvo21vakpph
        foreign key (league_id) references prediction_db.leagues (id),
    constraint FK2e8erbfecb0tjtq9iudg36bxu
        foreign key (away_team_id) references prediction_db.teams (id),
    constraint FK8k68nekawp47js52dq8720voe
        foreign key (home_team_id) references prediction_db.teams (id)
);

create table prediction_db.predictions
(
    id                     bigint auto_increment
        primary key,
    created_at             datetime(6)  null,
    is_calculated          bit          null,
    points_earned          int          null,
    predicted_away_score   int          null,
    predicted_first_scorer varchar(255) null,
    predicted_home_score   int          null,
    predicted_mvp          varchar(255) null,
    predicted_winner       varchar(255) null,
    match_id               bigint       null,
    user_id                bigint       null,
    breakdown              varchar(500) null,
    updated_at             datetime(6)  null,
    constraint FK5ehjwkl57ibsn56fjmwj892ju
        foreign key (user_id) references prediction_db.users (id),
    constraint FK5gyk6l61eh61hmb9u1mr6hd7v
        foreign key (match_id) references prediction_db.matches (id)
);

