package com.healthive;
import com.healthive.Config.AppConstants;
import com.healthive.Models.Role;
import com.healthive.Repository.RoleRepo;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
@SpringBootApplication
@RequiredArgsConstructor
@EnableAsync
@OpenAPIDefinition(info = @Info(title = "HealthHive: A health care Application",version = "3.0",description = "Complete APIs for Health Hive - An Health care application"))
@EnableCaching
public class HealtHiveApplication  implements CommandLineRunner {
	private final RoleRepo roleRepo;
	@Value("${ethereum.account.privateKey}")
	private String privateKey;
	@Value("${ethereum.network.url}")
	private String networkUrl;
	public static void main(String[] args) {
		SpringApplication.run(HealtHiveApplication.class, args);
	}
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}
	@Override
	public void run(String... args){
		try{
			Role role1 = new Role();
			role1.setId(AppConstants.ROLE_ADMIN);
			role1.setName("ROLE_ADMIN");

			Role role2 = new Role();
			role2.setId(AppConstants.ROLE_PATIENT);
			role2.setName("ROLE_NORMAL");

			Role role3 = new Role();
			role3.setId(AppConstants.ROLE_DOCTOR);
			role3.setName("ROLE_MERCHANT");

			List<Role> roles= List.of(role1, role2, role3);
			this.roleRepo.saveAll(roles);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
