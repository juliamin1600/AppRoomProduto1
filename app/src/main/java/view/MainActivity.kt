
package view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.unisanta.approomproduto.dao.ProductDao
import br.unisanta.approomproduto.databinding.ActivityMainBinding
import br.unisanta.approomproduto.db.AppDatabase
import br.unisanta.approomproduto.model.Product
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o banco de dados Room de forma recomendada (sem usar allowMainThreadQueries)
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration() // Usado para recriar o DB se o esquema mudar
            .build()

        productDao = db.productDao()

        // Exemplo de inserção de produto
        binding.btnInserirProduto.setOnClickListener {

            val name = binding.editProductName.text.toString().trim()
            val priceText = binding.editProductPrice.text.toString().trim()
            val description = binding.editProductDescription.text.toString()
            val quantityText =
                binding.editProductQuantity.text.toString().trim()

            // Verifica se os campos estão preenchidos
            if (name.isEmpty()) {
                binding.editProductName.error =
                    "Informe o nome do produto"
                return@setOnClickListener
            }

            // Valida o preço
            val price: Double? =
                priceText.replace(",", ".").toDoubleOrNull()

            if (price == null || price <= 0.0) {
                binding.editProductPrice.error =
                    "Informe um preço válido"
            }

            // Valida a quantidade
            val quantity: Int? = quantityText.toIntOrNull()

            if (quantity == null || quantity < 0) {
                binding.editProductQuantity.error =
                    "Informe uma quantidade válida"
            }

            binding.editProductName.error = null
            binding.editProductPrice.error = null
            binding.editProductQuantity.error = null

            val product = Product(
                name = name,
                price = price,
                description = description,
                quantity = quantity
            )

            // Insere o produto no banco de dados de forma assíncrona (usando corrotina)
            lifecycleScope.launch {
                try {
                    productDao.insertAll(product)

                    Log.i(
                        "TAG",
                        "Produto inserido: $name - R$$price"
                    )

                    Toast.makeText(
                        this@MainActivity,
                        "Produto inserido!",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.editProductName.setText("")
                    binding.editProductPrice.setText("")
                    binding.editProductDescription.setText("")
                    binding.editProductQuantity.setText("")

                } catch (e: Exception) {
                    Log.e("TAG", "Erro ao inserir produto", e)

                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao inserir produto.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Exemplo de recuperação de produtos
        binding.btnListarProdutos.setOnClickListener {
            // Recupera os produtos de forma assíncrona
            lifecycleScope.launch {
                val products = productDao.getAll()
                mostrarProdutos(products)
            }
        }

        // Exemplo de busca de produtos pelo nome
        binding.btnBuscar.setOnClickListener {
            val texto =
                binding.editBuscarNome.text.toString().trim()

            if (texto.isEmpty()) {
                binding.editBuscarNome.error =
                    "Digite um nome para buscar"
            }

            binding.editBuscarNome.error = null

            lifecycleScope.launch {
                val products =
                    productDao.buscarPorNome("%$texto%")

                if (products.isNotEmpty()) {
                    mostrarProdutos(
                        products,
                        "Resultados encontrados"
                    )
                } else {
                    binding.textListaProdutos.text =
                        "Nenhum produto encontrado com esse nome."
                }
            }
        }

        // Exemplo de exclusão de todos os produtos
        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()

                binding.textListaProdutos.text =
                    "Todos os produtos foram excluídos.\n" +
                            "Quantidade de produtos: 0\n" +
                            "Valor total do estoque: R$ 0,00"

                Toast.makeText(
                    this@MainActivity,
                    "Todos os produtos foram excluídos!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Exemplo de atualização do preço de um produto pelo ID
        binding.btnAtualizarProduto.setOnClickListener {

            val idTexto =
                binding.editAtualizarId.text.toString().trim()

            val precoTexto =
                binding.editNovoPreco.text.toString().trim()

            val id = idTexto.toIntOrNull()

            val novoPreco: Double? =
                precoTexto.replace(",", ".").toDoubleOrNull()

            if (id == null || id <= 0) {
                binding.editAtualizarId.error =
                    "Informe um ID válido"
            }

            if (novoPreco == null || novoPreco <= 0.0) {
                binding.editNovoPreco.error =
                    "Informe um preço válido"
            }

            binding.editAtualizarId.error = null
            binding.editNovoPreco.error = null

            lifecycleScope.launch {
                val products = productDao.getAll()
                val product = products.find { it.id == id }

                if (product != null) {
                    val produtoAtualizado = product.copy(
                        price = novoPreco
                    )

                    productDao.update(produtoAtualizado)

                    Toast.makeText(
                        this@MainActivity,
                        "Preço atualizado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.editAtualizarId.setText("")
                    binding.editNovoPreco.setText("")

                    // Atualiza a lista depois da alteração
                    mostrarProdutos(productDao.getAll())

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Produto não encontrado!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Exibe os produtos, a quantidade de registros e o total do estoque
    private fun mostrarProdutos(
        products: List<Product>,
        titulo: String = "Produtos cadastrados"
    ) {
        if (products.isNotEmpty()) {

            val quantidadeProdutos = products.size

            // Soma o preço de cada unidade em estoque
            val totalEstoque =
                products.sumOf { it.price * it.quantity }

            val lista = products.joinToString("\n\n") {
                "ID: ${it.id}\n" +
                        "Produto: ${it.name}\n" +
                        "Preço unitário: R$ %.2f".format(it.price) + "\n" +
                        "Descrição: ${it.description ?: "Sem descrição"}\n" +
                        "Quantidade em estoque: ${it.quantity}\n" +
                        "Subtotal: R$ %.2f".format(
                            it.price * it.quantity
                        )
            }

            binding.textListaProdutos.text =
                "$titulo: $quantidadeProdutos\n" +
                        "Valor total do estoque: R$ %.2f".format(
                            totalEstoque
                        ) +
                        "\n\n$lista"

        } else {
            binding.textListaProdutos.text =
                "Nenhum produto cadastrado.\n" +
                        "Quantidade de produtos: 0\n" +
                        "Valor total do estoque: R$ 0,00"
        }
    }
}
