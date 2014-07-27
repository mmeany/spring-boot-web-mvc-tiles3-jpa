spring-boot-web-mvc-tiles3-jpa
==============================

Spring Boot Web MVC configured to produce an executable WAR, demonstrating Tiles 3 and JPA configuration making use of HSQL database in file mode.

# Overview
This project builds upon the basic tiles 3 starting point, but adds in a Spring Data Repository for keeping track of people who visit the site.

# Configuration Notes
For configuration we bring in spring data jpa and a HSQL database, add some application properties defining the database and an SQL file to pre-populate it. Spring Boot will do the rest of the wiring up for us and we can start using repositories right from the off.

* Add dependencies to the POM to bring in Spring Data and the HSQL Database
```
        <!-- A database and JPA to access it -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.hsqldb</groupId>
            <artifactId>hsqldb</artifactId>
            <scope>runtime</scope>
        </dependency>
```
* Add database connection properties to application.properties resource
```
spring.jpa.hibernate.ddl-auto: create
spring.jpa.hibernate.naming_strategy: org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.database: HSQL
spring.jpa.show-sql: true

spring.datasource.url=jdbc:hsqldb:file:./target/testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driverClassName=org.hsqldb.jdbcDriver
```
* Create a file data.sql in resources folder, Spring will run this to pre-populate the database on startup.
```
INSERT INTO user_details (id, name, number_of_visits) VALUES (1, 'Mark', 27);
INSERT INTO user_details (id, name, number_of_visits) VALUES (2, 'Sara', 3);
INSERT INTO user_details (id, name, number_of_visits) VALUES (3, 'John', 14);
```
  * Note that you can start the program before creating this file to see the database structure.

# The Repository
Lets get straight to the fun part, creating a repository. To create a repository we need at least one Entity, a User was chosen for this example (can be extended for Spring Security later):
```
package com.mvmlabs.springboot.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_details")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer numberOfVisits;

    public User() {
    }

    public User(final String name) {
        this.name = name;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumberOfVisits() {
        return numberOfVisits == null ? 0 : numberOfVisits;
    }

    public void setNumberOfVisits(Integer numberOfVisits) {
        this.numberOfVisits = numberOfVisits;
    }
}
```
With an Entity to play with the repository is a piece of cake
```
package com.mvmlabs.springboot.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import com.mvmlabs.springboot.domain.User;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    User findByName(String name);
}
```
What does this buy us? Well, to start with notice that we are using a PagingAndSortingRepository. We need to know a little of its ancestory, so go look at the documentation and you will see it extends CrudRepository. Yep, there are methods for fetching and saving entities. Since this is a Paging and Sorting repository it knows how to handle Pageable as well making navigating through a large data collection easier.

The repository created has a single method, findByName, included to demonstrate the query DSL provided by Spring Data; the method name is the DSL used to create the query. In this case, the resulting query would find the user with the name provided.

In this sample application a service layer was introduced. Why? Because I like service layers and I like keeping MVC Controller logic to a minimum. The transaction boundaries were added to the service implementations. Take a look at UserServiceJpaImpl:

```
package com.mvmlabs.springboot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mvmlabs.springboot.dao.UserRepository;
import com.mvmlabs.springboot.domain.User;
import com.mvmlabs.springboot.service.UserService;

@Service
@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
public class UserServiceJpaImpl implements UserService {

    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceJpaImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public User registerVisit(final String name) {
        
        User user = userRepository.findByName(name);
        if (user == null) {
            user = new User(name);
        }
        user.setNumberOfVisits(user.getNumberOfVisits() + 1);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly=true)
    public Page<User> getAllRegisteredUsers(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
```

To note:
* The repository is injected into the service
* All methods are Transactional, the list method being marked readonly
* registerVisit() makes use of the findByName() repository method and also the CRUD save() method. We never had to code these!
* getAllRegisteredUsers() makes use of Pageable to allow paging through the tables data.

Notice that more work was required creating the Entity than the repository that interacts with the database. We like this!

# The Controller
For simplicity everything is happening in a single controller; GreetingController. Spring Boot has done a lot of configuration for us such as registered mappers for Spring Data Pageable. 

```
package com.mvmlabs.springboot.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mvmlabs.springboot.service.UserService;

@Controller
public class GreetingController {
    private Log log = LogFactory.getLog(this.getClass());

    private final UserService userService;
    
    @Autowired
    public GreetingController(final UserService userService) {
        this.userService = userService;
    }
    
    @RequestMapping(value = "/home", method=RequestMethod.GET)
    public String home() {
        return "site.homepage";
    }
    
    @RequestMapping(value = "/greet", method=RequestMethod.GET)
    public ModelAndView greet(@RequestParam(value = "name", required=false, defaultValue="World!")final String name, final Model model) {
        log.info("Controller has been invoked with Request Parameter name = '" + name + "'.");
        return registerRequest(name);
    }

    @RequestMapping(value = "/greet/{name}", method=RequestMethod.GET)
    public ModelAndView greetTwoWays(@PathVariable(value="name") final String name, final Model model) {
        log.info("Controller has been invoked with Path Variable name = '" + name + "'.");
        return registerRequest(name);
    }
    
    @RequestMapping(value = "/list", method=RequestMethod.GET)
    public ModelAndView list(@PageableDefault(page = 0, value = 5) final Pageable pageable) {
        return new ModelAndView("site.list", "page", userService.getAllRegisteredUsers(pageable));
    }
    
    private ModelAndView registerRequest(final String name) {
        return new ModelAndView("site.greeting", "user", userService.registerVisit(name));
    }
}
```
To note:
* UserService is injected into the constructor (can use a private final field)
* The list() method makes use of Pageable to allow a Page of results to be fetched
* The list() method Pageable parameter is annotated with PageableDefault to set initial page number and page size

One thing to keep in mind about the Pageable mechanism is that page number is zero based, but usually we show the user a 1 based list.

How do we use Page and Pageable in a JSP? look at list.jsp:
```
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div>
    <table>
        <caption>Visitor List</caption>
        <thead>
            <tr>
                <th>Name</th>
                <th>Number of visits</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${page.content}">
            <tr>
                <td>${user.name}</td>
                <td>${user.numberOfVisits}</td>
            </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
<div>Viewing page ${page.number + 1} of ${page.totalPages}</div>
<div>
    <c:forEach var="i" begin="1" end="${page.totalPages}">
    <c:choose>
        <c:when test="${i == page.number + 1}">${i}</c:when>
        <c:otherwise><a href="?page=${i - 1}">${i}</a></c:otherwise>
    </c:choose>
    </c:forEach>
</div>
```
To note:
* page.content is used to get to the list of data contained in this page
* page.number and page.totalPages are used to present a navigation list.

I realise that the navigation list is naff and that if there were lots of pages then a long list of URLs would be displayed. An exercise for the reader then; precalculate the begin and end values to always show at most 5 pages each side of the current page. BTW the pagination DIV is an ideal candidate for a custom tag (more on that later when we get to Bootstrap).

# Try it out
All thats left to do now is build and run it.
```
mvn clean package
java -jar target\spring-boot-web-mvc-tiles3-1.0.war --debug
```

Some URLs to try:
* http://localhost:8080/index.html
* http://localhost:8080/list
* http://localhost:8080/list?page=0
* http://localhost:8080/list?page=0&size=10
  * Note the use of size, this is the parameter mapped to Pageable by Spring
* http://localhost:8080/greet/Mark
* http://localhost:8080/greet/[YOUR_NAME]

What fun :)



