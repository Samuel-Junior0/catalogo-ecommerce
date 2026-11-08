package br.edu.ifpb.catalogo_ecommerce.service;

import br.edu.ifpb.catalogo_ecommerce.model.Verificacao.CodigoVerificacao;
import br.edu.ifpb.catalogo_ecommerce.repository.CodigoVerificacaoRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class VerificacaoEmailService {

    private static final Duration VALIDADE = Duration.ofMinutes(10);
    private static final int MAX_TENTATIVAS = 5;

    private final SecureRandom random = new SecureRandom();
    private final JavaMailSender mailSender;
    private final CodigoVerificacaoRepository repository;

    public VerificacaoEmailService(JavaMailSender mailSender, CodigoVerificacaoRepository repository) {
        this.mailSender = mailSender;
        this.repository = repository;
    }

    public void enviarCodigo(String email) {
        email = email.trim().toLowerCase();

        String codigo = String.format("%06d", random.nextInt(1_000_000)); // 000000 a 999999

        CodigoVerificacao registro = new CodigoVerificacao();
        registro.setCodigoHash(hash(email, codigo));
        registro.setExpiraEmMillis(System.currentTimeMillis() + VALIDADE.toMillis());
        registro.setTentativas(0);
        repository.salvarComId(email, registro);   // sobrescreve código anterior

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(email);
        mensagem.setSubject("Seu código de verificação");
        mensagem.setText("Seu código é: " + codigo
                + "\n\nEle expira em " + VALIDADE.toMinutes() + " minutos."
                + "\nSe você não solicitou, ignore este email.");
        mailSender.send(mensagem);
    }

    public boolean verificarCodigo(String email, String codigoInformado) {
        email = email.trim().toLowerCase();

        CodigoVerificacao registro = repository.buscarPorId(email).orElse(null);
        if (registro == null) {
            return false;                           // nunca pediu código, ou já foi usado
        }
        if (System.currentTimeMillis() > registro.getExpiraEmMillis()) {
            repository.deletar(email);
            return false;                           // expirou
        }
        if (registro.getTentativas() >= MAX_TENTATIVAS) {
            return false;                           // bloqueado: peça um novo código
        }

        boolean correto = MessageDigest.isEqual(
                hash(email, codigoInformado).getBytes(StandardCharsets.UTF_8),
                registro.getCodigoHash().getBytes(StandardCharsets.UTF_8));

        if (!correto) {
            registro.setTentativas(registro.getTentativas() + 1);
            repository.salvarComId(email, registro);
            return false;
        }

        repository.deletar(email);                  // código só vale uma vez
        return true;
    }

    private String hash(String email, String codigo) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest((email + ":" + codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}