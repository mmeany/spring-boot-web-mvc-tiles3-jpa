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

/**
 * Controller that demonstrates:
 *  tiles mapping,
 *  request parameters and path variables.
 *  Constructor injection of autowired services
 * 
 * @author Mark Meany
 */
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
