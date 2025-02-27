package com.inkomoko.smarttask.hibernate;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.sql.DataSource;

//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = {
//                "com.inkomoko.smarttask.email",
//                "com.inkomoko.smarttask.User"
//        },
//        entityManagerFactoryRef = "coreEntityManagerFactory",
//        transactionManagerRef = "coreTransactionManager"
//)
public class CoreDatabaseConfiguration {

    public static String dataSourcePrefix = "java:/";


//    @Bean(name = "coreDataSource")
//    @Primary
//    public DataSource dataSource() throws NamingException {
//        JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
//        return dataSourceLookup.getDataSource(String.format("%s%s", dataSourcePrefix, "smart-task"));
//    }
//
//    @Primary
//    @Bean(name = "coreEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) throws NamingException {
//        String[] modelLocations = {
//                "com.inkomoko.smarttask.User.models",
//                "com.inkomoko.smarttask.security.models",
//                "com.inkomoko.smarttask.email.models",
//        };
//
//        LocalContainerEntityManagerFactoryBean theBean = builder.dataSource(dataSource()).packages(modelLocations).persistenceUnit("guzo").build();
//        theBean.getJpaPropertyMap().put("spring.jpa.database-platform", "org.hibernate.dialect.MySQL8Dialect");
//        return theBean;
//    }

    @Bean(name="uatDataSource")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    //local
    @Primary
    @Bean(name = "coreEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("uatDataSource") DataSource dataSource) throws NamingException {

        String[] modelLocations = {
                "com.inkomoko.smarttask.User.models",
                "com.inkomoko.smarttask.security.models",
                "com.inkomoko.smarttask.email.models",
        };

        LocalContainerEntityManagerFactoryBean theBean = builder.dataSource(dataSource).packages(modelLocations).persistenceUnit("smart_task").build();
        return theBean;
    }

    @Primary
    @Bean(name = "coreTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("coreEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}