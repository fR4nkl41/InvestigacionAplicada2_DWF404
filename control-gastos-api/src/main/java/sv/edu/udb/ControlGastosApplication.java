package sv.edu.udb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sv.edu.udb.model.Categoria;
import sv.edu.udb.repository.CategoriaRepository;

@SpringBootApplication
public class ControlGastosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlGastosApplication.class, args);
	}

	// Datos de prueba iniciales
	@Bean
	CommandLineRunner initData(CategoriaRepository categoriaRepository) {
		return args -> {
			if (categoriaRepository.count() == 0) {
				categoriaRepository.save(Categoria.builder()
						.nombre("Salario")
						.tipo("INGRESO")
						.build());
				categoriaRepository.save(Categoria.builder()
						.nombre("Comida")
						.tipo("GASTO")
						.build());
				categoriaRepository.save(Categoria.builder()
						.nombre("Transporte")
						.tipo("GASTO")
						.build());
				System.out.println("Categorías de prueba insertadas");
			}
		};
	}
}
