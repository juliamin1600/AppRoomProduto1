AppRoomProduto: Cadastro de Produtos com Room SQLite

Aluno(a): Julia Oliveira Mineiro - Guilherme Caires Lobo
RA:            241987            -        232257
Disciplina: Desenvolvimento para Dispositivos Móveis
Professor(a): Alessandro

Sobre o projeto

Aplicativo Android desenvolvido em Kotlin que permite cadastrar, listar, buscar, atualizar e excluir produtos, utilizando persistência local com Room (SQLite).
O aplicativo armazena o nome, o preço, a descrição e a quantidade em estoque de cada produto. Também permite consultar os produtos cadastrados e calcular o valor total do estoque.

Tecnologias
Kotlin
Room 2.6.1 (SQLite)
ViewBinding
Corrotinas (lifecycleScope)
XML para construção da interface
Prints do app

!mainscreen.PNG

!logcat

Parte A: Complemento prático
A1. Limpar campos após inserir
O que foi feito: Após inserir um produto com sucesso, os campos são limpos e o foco retorna ao campo Nome.

Trecho de código:
binding.editProductName.text.clear()
binding.editProductPrice.text.clear()
binding.editProductDescription.text.clear()
binding.editProductQuantity.text.clear()

binding.editProductName.requestFocus()


Print:

!uvaantes;uvadepois

A2. Validação de preço
O que foi feito: Foi implementada uma validação para impedir o cadastro de produtos com preço vazio, inválido, zero ou negativo. A mensagem de erro é exibida diretamente no campo de preço.

Trecho de código:
val price = priceText.replace(",", ".").toDoubleOrNull()
if (price == null || price <= 0.0) {
binding.editProductPrice.error = "Informe um preço válido"
return@setOnClickListener
}


Print:

!sempreco

A3. Lista de produtos na tela

O que foi feito: Foi utilizado um TextView chamado textListaProdutos para exibir os produtos cadastrados ao clicar no botão Listar Produtos.
Trecho de código:
binding.btnListarProdutos.setOnClickListener {
lifecycleScope.launch {
val products = productDao.getAll()
mostrarProdutos(products)
}
}


Print:

!2produtos

A4. Total do estoque
O que foi feito: A aplicação mostra a quantidade de produtos cadastrados e calcula o valor total do estoque considerando o preço unitário e a quantidade de cada produto.

Trecho de código:
val quantidadeProdutos = products.size
val totalEstoque = products.sumOf {
it.price * it.quantity
}


Print:

!2produtos

A5. Buscar por nome
O que foi feito: Foi implementada uma busca por nome utilizando uma consulta SQL com LIKE. O usuário pode digitar parte do nome para encontrar produtos correspondentes.

Trecho de código do DAO:
@Query("SELECT * FROM product WHERE name LIKE :nome")
suspend fun buscarPorNome(nome: String): List<Product>


Trecho de código da busca:
val texto = binding.editBuscarNome.text.toString().trim()
val products = productDao.buscarPorNome("%$texto%")


Print:

!busca1

A6. Excluir produtos
O que foi feito: Foi implementada a exclusão de todos os produtos cadastrados. Também foi criada uma função no DAO para permitir a exclusão individual de um produto.

Trecho de código do DAO:
@Query("DELETE FROM product")
suspend fun deleteAll()
@Delete
suspend fun deleteProduct(product: Product)


Print:

!nenhumitem

A7. Atualizar produto
O que foi feito: Foi implementada a atualização do preço de um produto existente por meio do seu ID. Os demais dados são preservados.

Trecho de código do DAO:
@Update
suspend fun update(product: Product)


Trecho de código da atualização:
val produtoAtualizado = product.copy(
price = novoPreco
)

productDao.update(produtoAtualizado)


Print:

!novopreco

A8. Novo campo na tabela: quantidade em estoque
O que foi feito: Foi adicionado o atributo quantity, do tipo Int, à entidade Product, além de um campo no layout para informar a quantidade em estoque. A versão do banco de dados Room foi alterada para 2.

Trecho de código da entidade:
@ColumnInfo(name = "quantity")
val quantity: Int


Trecho de código do banco de dados:
@Database(
entities = [Product::class],
version = 2,
exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
abstract fun productDao(): ProductDao
}


O que aconteceu com os produtos já cadastrados?
Como foi utilizada a configuração fallbackToDestructiveMigration() e não foi criada uma migração específica entre as versões, o Room pode apagar e recriar as tabelas quando detecta a mudança de estrutura. Por isso, os produtos anteriormente cadastrados podem ser perdidos. Isso acontece porque a tabela antiga não possuía a coluna quantity.

Print:

!10frutas

Parte B: Questões dissertativas
B1. O que é persistência de dados?
Resposta: Persistência de dados é a capacidade de armazenar informações de forma que elas continuem disponíveis mesmo depois que o aplicativo é fechado. Neste projeto, os produtos são armazenados localmente em um banco SQLite por meio do Room.

B2. O que é o Room?
Resposta: Room é uma biblioteca do Android que facilita o acesso a bancos de dados SQLite. Ela permite definir entidades, consultas e operações de inserção, atualização e exclusão usando classes e interfaces Kotlin.

B3. Qual é a função da entidade Product?
Resposta: A entidade Product representa a tabela de produtos no banco de dados. Seus atributos, como id, name, price, description e quantity, representam as informações armazenadas em cada registro.

B4. Qual é a função do DAO?
Resposta: DAO significa Data Access Object. É a interface responsável por definir as operações realizadas no banco de dados, como consultar, inserir, buscar, atualizar e excluir produtos. No projeto, essas operações são declaradas com anotações como @Query, @Insert, @Update e @Delete.

B5. Para que serve o ViewBinding?
Resposta: ViewBinding permite acessar os componentes definidos no XML por meio de referências tipadas, sem precisar utilizar repetidamente findViewById(). Neste projeto, ele é utilizado para acessar campos de texto, botões e o componente que apresenta a lista de produtos.

B6. Por que utilizar corrotinas e lifecycleScope?
Resposta: As corrotinas permitem executar operações assíncronas, como consultas e gravações no banco de dados, sem bloquear a interface do aplicativo. O lifecycleScope associa essas tarefas ao ciclo de vida da Activity, ajudando a evitar que continuem executando depois que ela é destruída.

B7. Qual é a diferença entre @Insert, @Update e @Delete?
Resposta: A anotação @Insert é utilizada para inserir novos registros no banco. A anotação @Update atualiza um registro existente com base em sua chave primária. Já a anotação @Delete remove do banco o registro correspondente ao objeto informado.

B8. Por que é necessário atualizar a versão do banco de dados?
Resposta: A versão do banco precisa ser atualizada quando ocorrem mudanças no esquema, como a inclusão de uma nova coluna na entidade Product. Neste projeto, a versão foi alterada para 2 após a inclusão do campo quantity. Como foi utilizada a migração destrutiva, os dados antigos podem ser apagados quando o banco é recriado. Em um aplicativo real, uma migração que preserve os dados pode ser mais adequada.

Como executar
Clone o repositório para o computador.
Abra o projeto no Android Studio.
Aguarde a sincronização do Gradle.
Execute o aplicativo em um emulador ou dispositivo Android compatível, com API 24 ou superior.
Utilize a tela inicial para cadastrar, listar, buscar, atualizar e excluir produtos.
Conclusão

O projeto permitiu praticar o desenvolvimento Android com Kotlin e a utilização do Room para persistência local de dados. Foram implementadas funcionalidades de cadastro, listagem, busca, atualização e exclusão, além da validação de preço e do cálculo do valor total do estoque."# AppRoomProduto" 
