import java.util.InputMismatchException; // Embora não usemos try-catch, deixo o import caso seja útil saber qual exceção era tratada
import java.util.Random;
import java.util.Scanner;

public class BatalhaNaval {

    // --- CONSTANTES ---
    private static final int TAMANHO_MAPA = 10;
    private static final char AGUA = '~';
    private static final char NAVIO = 'N';
    private static final char TIRO_AGUA = 'o';
    private static final char TIRO_CERTO = 'X';
    private static final char NAVIO_ATINGIDO = 'X';

    private static final int[] TAMANHOS_NAVIO = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    private static final String[] NOMES_NAVIO = {"Porta-Aviões (4)", "Navio-Tanque (3)", "Navio-Tanque (3)",
            "Contratorpedeiro (2)", "Contratorpedeiro (2)", "Contratorpedeiro (2)",
            "Submarino (1)", "Submarino (1)", "Submarino (1)", "Submarino (1)"};

    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();


    // --- METODO PRINCIPAL ---
    public static void main(String[] args) {
        exibirBoasVindas();

        int modoJogo = escolherModoJogo();
        boolean contraComputador = (modoJogo == 1);

        // Obter nomes dos jogadores
        System.out.print("Digite o nome do Jogador 1: ");
        String nomeJogador1 = scanner.nextLine();
        String nomeJogador2 = contraComputador ? "Computador" : pedirNomeJogador2();

        // Inicializar mapas
        char[][] mapaJogador1 = new char[TAMANHO_MAPA][TAMANHO_MAPA];
        char[][] mapaAtaqueJogador1 = new char[TAMANHO_MAPA][TAMANHO_MAPA]; // Mapa que J1 usa para atacar J2
        char[][] mapaJogador2 = new char[TAMANHO_MAPA][TAMANHO_MAPA];
        char[][] mapaAtaqueJogador2 = new char[TAMANHO_MAPA][TAMANHO_MAPA]; // Mapa que J2 usa para atacar J1

        inicializarMapa(mapaJogador1);
        inicializarMapa(mapaAtaqueJogador1);
        inicializarMapa(mapaJogador2);
        inicializarMapa(mapaAtaqueJogador2);

        // Fase de Alocação
        System.out.println("\n--- Fase de Alocação: " + nomeJogador1 + " ---");
        alocarFrota(mapaJogador1, nomeJogador1, false); // Jogador 1 sempre aloca manualmente ou automaticamente

        System.out.println("\n--- Fase de Alocação: " + nomeJogador2 + " ---");
        alocarFrota(mapaJogador2, nomeJogador2, contraComputador); // Computador sempre aloca automaticamente

        // Fase de Batalha
        System.out.println("\n--- Que Comece a Batalha! ---");
        jogarBatalha(mapaJogador1, mapaAtaqueJogador1, nomeJogador1,
                mapaJogador2, mapaAtaqueJogador2, nomeJogador2,
                contraComputador);

        scanner.close();
        System.out.println("\nObrigado por jogar Batalha Naval!");
    }

    // --- Métodos de Interface e Utilidade ---

    private static void exibirBoasVindas() {
        System.out.println("======================================");
        System.out.println(" Bem-vindo ao Batalha Naval em Java! ");
        System.out.println("======================================");
    }

    private static int escolherModoJogo() {
        int escolha = 0;
        boolean entradaValida = false;
        do {
            System.out.println("\nEscolha o modo de jogo:");
            System.out.println("1. Jogador vs Computador");
            System.out.println("2. Jogador vs Jogador");
            System.out.print("Opção: ");
            String input = scanner.nextLine().trim(); // Lê a linha inteira como String

            if (input.equals("1")) {
                escolha = 1;
                entradaValida = true;
            } else if (input.equals("2")) {
                escolha = 2;
                entradaValida = true;
            } else {
                System.out.println("Entrada inválida. Por favor, digite 1 ou 2.");
                // entradaValida continua false, o loop repetirá
            }
        } while (!entradaValida);
        // Não é mais necessário consumir nova linha, pois usamos nextLine()
        return escolha;
    }

    private static String pedirNomeJogador2() {
        System.out.print("Digite o nome do Jogador 2: ");
        return scanner.nextLine();
    }


    private static void inicializarMapa(char[][] mapa) {
        for (int i = 0; i < TAMANHO_MAPA; i++) {
            for (int j = 0; j < TAMANHO_MAPA; j++) {
                mapa[i][j] = AGUA;
            }
        }
    }

    private static void exibirMapa(char[][] mapa, boolean esconderNavios) {
        System.out.print("   "); // Espaçamento para os números das linhas
        for (int i = 0; i < TAMANHO_MAPA; i++) {
            System.out.print((char) ('A' + i) + " "); // Letras das colunas
        }
        System.out.println();

        for (int i = 0; i < TAMANHO_MAPA; i++) {
            System.out.printf("%2d ", i); // Números das linhas (com formatação)
            for (int j = 0; j < TAMANHO_MAPA; j++) {
                char caractere = mapa[i][j];
                if (esconderNavios && caractere == NAVIO) {
                    System.out.print(AGUA + " ");
                } else {
                    System.out.print(caractere + " ");
                }
            }
            System.out.println();
        }
    }

    private static void exibirMapasJogo(char[][] meuMapa, char[][] mapaAtaque, String meuNome, String nomeOponente) {
        System.out.println("\n--- Mapa de " + meuNome + " (Seus Navios) ---");
        exibirMapa(meuMapa, false); // Mostra seus próprios navios
        System.out.println("\n--- Mapa de Ataque (Visão de " + nomeOponente + ") ---");
        exibirMapa(mapaAtaque, true); // Esconde navios não atingidos do oponente
    }

    private static int[] converterCoordenada(String coordStr) {
        if (coordStr == null || coordStr.length() < 2) {
            return null;
        }
        coordStr = coordStr.toUpperCase(); // Garante que a letra seja maiúscula

        char letraColuna = coordStr.charAt(0);
        String parteNumero = coordStr.substring(1);

        if (letraColuna < 'A' || letraColuna >= ('A' + TAMANHO_MAPA)) {
            return null; // Letra fora do intervalo
        }

        int coluna = letraColuna - 'A'; // Converte A->0, B->1, etc.
        int linha = -1; // Inicializa com valor inválido

        // Validação manual do número antes de converter
        boolean numeroValido = true;
        if (parteNumero.isEmpty()) {
            numeroValido = false;
        } else {
            for (char c : parteNumero.toCharArray()) {
                if (!Character.isDigit(c)) {
                    numeroValido = false;
                    break;
                }
            }
        }

        if (numeroValido) {
            linha = Integer.parseInt(parteNumero); // Agora a conversão é segura
            if (linha < 0 || linha >= TAMANHO_MAPA) {
                return null; // Número da linha fora do intervalo
            }
        } else {
            return null; // Formato numérico inválido
        }

        return new int[]{linha, coluna};
    }

    private static int[] lerCoordenadaUsuario(String prompt) {
        int[] coordenada = null;
        do {
            System.out.print(prompt + " (Ex: A0, J9): ");
            String input = scanner.nextLine().trim();
            coordenada = converterCoordenada(input);
            if (coordenada == null) {
                System.out.println("Coordenada inválida. Use o formato LetraNumero (A0 a "
                        + (char)('A' + TAMANHO_MAPA - 1) + (TAMANHO_MAPA - 1) + ").");
            }
        } while (coordenada == null);
        return coordenada;
    }

    private static boolean lerOrientacaoUsuario() {
        String orientacao;
        do {
            System.out.print("Orientação (H para Horizontal, V para Vertical): ");
            orientacao = scanner.nextLine().trim().toUpperCase();
            if (!orientacao.equals("H") && !orientacao.equals("V")) {
                System.out.println("Orientação inválida. Digite H ou V.");
            }
        } while (!orientacao.equals("H") && !orientacao.equals("V"));
        return orientacao.equals("H");
    }

    // --- Métodos de Alocação de Navios ---

    private static void alocarFrota(char[][] mapa, String nomeJogador, boolean ehComputador) {
        if (ehComputador) {
            System.out.println(nomeJogador + " está alocando a frota automaticamente...");
            alocarNaviosAutomatico(mapa);
            System.out.println("Frota do " + nomeJogador + " alocada.");
        } else {
            int escolha = 0;
            boolean entradaValida = false;
            do {
                System.out.println("\n" + nomeJogador + ", como deseja alocar seus navios?");
                System.out.println("1. Manualmente");
                System.out.println("2. Automaticamente");
                System.out.print("Opção: ");
                String input = scanner.nextLine().trim(); // Lê como String

                if (input.equals("1")) {
                    escolha = 1;
                    entradaValida = true;
                } else if (input.equals("2")) {
                    escolha = 2;
                    entradaValida = true;
                } else {
                    System.out.println("Entrada inválida. Por favor, digite 1 ou 2.");
                    // entradaValida continua false
                }
            } while (!entradaValida);
            // Não precisa consumir nova linha

            if (escolha == 1) {
                alocarNaviosManual(mapa, nomeJogador);
            } else {
                System.out.println(nomeJogador + " está alocando a frota automaticamente...");
                alocarNaviosAutomatico(mapa);
                System.out.println("\nFrota alocada automaticamente para " + nomeJogador + ":");
                exibirMapa(mapa, false); // Mostra o resultado da alocação automática
            }
        }
        // Pausa removida
    }

    private static void alocarNaviosManual(char[][] mapa, String nomeJogador) {
        System.out.println("\n" + nomeJogador + ", aloque sua frota:");
        exibirMapa(mapa, false);

        for (int i = 0; i < TAMANHOS_NAVIO.length; i++) {
            int tamanho = TAMANHOS_NAVIO[i];
            String nomeNavio = NOMES_NAVIO[i];
            boolean alocado = false;
            do {
                System.out.println("\nAlocando: " + nomeNavio);
                int[] coordenada = lerCoordenadaUsuario("Digite a coordenada inicial (superior ou esquerda)");
                boolean horizontal = lerOrientacaoUsuario();

                // Tenta validar e alocar na posição escolhida
                if (validarPosicaoEAlocar(mapa, tamanho, coordenada[0], coordenada[1], horizontal)) {
                    alocado = true;
                    System.out.println(nomeNavio + " alocado com sucesso!");
                    exibirMapa(mapa, false); // Mostra o mapa atualizado
                } else {
                    System.out.println("Posição inválida! O navio não cabe ou sobrepõe outro. Tente novamente.");
                }
            } while (!alocado);
        }
        System.out.println("\nFrota completa alocada manually por " + nomeJogador + "!");
    }

    private static void alocarNaviosAutomatico(char[][] mapa) {
        for (int tamanho : TAMANHOS_NAVIO) {
            boolean alocado = false;
            do {
                int linha = random.nextInt(TAMANHO_MAPA);
                int coluna = random.nextInt(TAMANHO_MAPA);
                boolean horizontal = random.nextBoolean();

                // Tenta validar e alocar na posição aleatória
                if (validarPosicaoEAlocar(mapa, tamanho, linha, coluna, horizontal)) {
                    alocado = true;
                }
                // Se não conseguiu, o loop continua tentando novas posições aleatórias
            } while (!alocado);
        }
    }

    private static boolean validarPosicaoEAlocar(char[][] mapa, int tamanho, int linha, int coluna, boolean horizontal) {
        // 1. Verificar Limites do Mapa
        if (horizontal) {
            if (coluna + tamanho > TAMANHO_MAPA) return false; // Estoura pela direita
        } else {
            if (linha + tamanho > TAMANHO_MAPA) return false; // Estoura por baixo
        }

        // 2. Verificar Sobreposição
        for (int i = 0; i < tamanho; i++) {
            int l = horizontal ? linha : linha + i;
            int c = horizontal ? coluna + i : coluna;
            // Verificação adicional de segurança (embora limites já checados)
            if (l < 0 || l >= TAMANHO_MAPA || c < 0 || c >= TAMANHO_MAPA) return false;

            if (mapa[l][c] != AGUA) return false; // Já tem algo aqui (outro navio)
        }

        // 3. Se chegou aqui, a posição é válida. Alocar o navio.
        for (int i = 0; i < tamanho; i++) {
            int l = horizontal ? linha : linha + i;
            int c = horizontal ? coluna + i : coluna;
            mapa[l][c] = NAVIO;
        }
        return true; // Alocado com sucesso
    }

    // --- Métodos da Fase de Batalha ---

    private static void jogarBatalha(char[][] mapaJ1, char[][] mapaAtaqueJ1, String nomeJ1,
                                     char[][] mapaJ2, char[][] mapaAtaqueJ2, String nomeJ2,
                                     boolean contraComputador) {
        boolean jogador1Vez = true; // Jogador 1 começa
        boolean jogoTerminou = false;
        String vencedor = "";

        while (!jogoTerminou) {
            String nomeAtacante, nomeDefensor;
            char[][] mapaAtaqueAtual;
            char[][] mapaDefesaOponente;
            char[][] meuMapaAtual; // Mapa do atacante com seus navios

            if (jogador1Vez) {
                nomeAtacante = nomeJ1;
                nomeDefensor = nomeJ2;
                mapaAtaqueAtual = mapaAtaqueJ1; // J1 ataca usando seu mapa de ataque
                mapaDefesaOponente = mapaJ2;    // Verifica acerto no mapa de defesa de J2
                meuMapaAtual = mapaJ1;
            } else {
                nomeAtacante = nomeJ2;
                nomeDefensor = nomeJ1;
                mapaAtaqueAtual = mapaAtaqueJ2; // J2 ataca usando seu mapa de ataque
                mapaDefesaOponente = mapaJ1;    // Verifica acerto no mapa de defesa de J1
                meuMapaAtual = mapaJ2;
            }

            System.out.println("\n--------------------------------------");
            System.out.println("Vez de: " + nomeAtacante);
            exibirMapasJogo(meuMapaAtual, mapaAtaqueAtual, nomeAtacante, nomeDefensor);

            boolean jogadaValida;
            boolean acertou;
            do {
                jogadaValida = true;
                acertou = false;
                int[] coordenadaAtaque;

                // Obter coordenada de ataque (do jogador ou do PC)
                if (!jogador1Vez && contraComputador) {
                    System.out.println(nomeAtacante + " está pensando...");
                    // Pausa removida
                    coordenadaAtaque = jogadaComputador(mapaAtaqueAtual); // PC escolhe onde atirar
                    System.out.println(nomeAtacante + " atira em " + (char)('A' + coordenadaAtaque[1]) + coordenadaAtaque[0]);
                } else {
                    coordenadaAtaque = lerCoordenadaUsuario("Digite a coordenada para atacar");
                }

                int linha = coordenadaAtaque[0];
                int coluna = coordenadaAtaque[1];

                // Processar a jogada
                char resultadoTiro = processarJogada(mapaAtaqueAtual, mapaDefesaOponente, linha, coluna);

                switch (resultadoTiro) {
                    case TIRO_AGUA:
                        System.out.println("Splash! Tiro na água.");
                        acertou = false;
                        break;
                    case TIRO_CERTO:
                        System.out.println("BOOM! Acertou um navio!");
                        acertou = true; // Jogador joga novamente
                        // Verificar se o jogo acabou após o acerto
                        if (verificarFimDeJogo(mapaDefesaOponente)) {
                            jogoTerminou = true;
                            vencedor = nomeAtacante;
                        }
                        break;
                    case 'R': // Repetir jogada (já atirou ali)
                        System.out.println("Você já atirou nesta posição. Tente novamente.");
                        jogadaValida = false; // Força repetir o loop da jogada
                        break;
                }
                // Pausa removida

            } while (!jogadaValida || (acertou && !jogoTerminou)); // Repete se jogada inválida OU se acertou e o jogo não acabou

            // Troca o turno apenas se a jogada foi válida e não resultou em acerto (ou se o jogo acabou)
            if (!acertou && !jogoTerminou) {
                jogador1Vez = !jogador1Vez;
            }
        }

        // Fim do Jogo
        System.out.println("\n======================================");
        System.out.println("          FIM DE JOGO! ");
        System.out.println(" O vencedor é: " + vencedor + "!");
        System.out.println("======================================");

        // Exibir mapas finais
        System.out.println("\n--- Mapa Final de " + nomeJ1 + " ---");
        exibirMapa(mapaJ1, false);
        System.out.println("\n--- Mapa Final de " + nomeJ2 + " ---");
        exibirMapa(mapaJ2, false);
    }

    private static char processarJogada(char[][] mapaAtaqueAtual, char[][] mapaDefesaOponente, int linha, int coluna) {
        // Verificar se já atirou nesta posição (no mapa de ataque)
        if (mapaAtaqueAtual[linha][coluna] == TIRO_AGUA || mapaAtaqueAtual[linha][coluna] == TIRO_CERTO) {
            return 'R'; // Indicar que deve repetir a jogada
        }

        // Verificar o que tem no mapa de defesa do oponente
        if (mapaDefesaOponente[linha][coluna] == NAVIO) {
            mapaAtaqueAtual[linha][coluna] = TIRO_CERTO;   // Marca acerto no mapa de ataque
            mapaDefesaOponente[linha][coluna] = NAVIO_ATINGIDO; // Marca acerto no mapa de defesa
            return TIRO_CERTO;
        } else { // Era água
            mapaAtaqueAtual[linha][coluna] = TIRO_AGUA;   // Marca erro no mapa de ataque
            // Não precisa mudar mapaDefesaOponente se era água
            return TIRO_AGUA;
        }
    }

    private static boolean verificarFimDeJogo(char[][] mapaDefesa) {
        for (int i = 0; i < TAMANHO_MAPA; i++) {
            for (int j = 0; j < TAMANHO_MAPA; j++) {
                if (mapaDefesa[i][j] == NAVIO) {
                    return false; // Encontrou um pedaço de navio não atingido
                }
            }
        }
        return true; // Não encontrou nenhum pedaço de navio não atingido
    }

    private static int[] jogadaComputador(char[][] mapaAtaquePC) {
        int linha, coluna;
        do {
            linha = random.nextInt(TAMANHO_MAPA);
            coluna = random.nextInt(TAMANHO_MAPA);
        } while (mapaAtaquePC[linha][coluna] == TIRO_AGUA || mapaAtaquePC[linha][coluna] == TIRO_CERTO); // Repete se já atirou ali

        return new int[]{linha, coluna};

    }
}