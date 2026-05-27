resource "aws_instance" "devops_server" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = var.subnet_id
  vpc_security_group_ids = [var.security_group_id]

  # Cargamos el script de inicialización desde el template decoupling
  user_data = templatefile("${path.module}/templates/k3s-server.sh.tpl", {
    eviction_threshold = "500Mi"
  })

  root_block_device {
    volume_size           = 30 # Ampliado a 30GB (Límite máximo Free Tier) para soportar Sonar y Observabilidad
    volume_type           = "gp3"
    delete_on_termination = true
  }

  tags = {
    Name        = "NTT-Reto-DevOps-Pro"
    Environment = var.environment
  }
}