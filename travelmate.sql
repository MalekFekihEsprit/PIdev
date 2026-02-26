-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Feb 26, 2026 at 08:52 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `travelmate`
--

-- --------------------------------------------------------

--
-- Table structure for table `activites`
--

CREATE TABLE `activites` (
  `id` int(11) NOT NULL,
  `nom` varchar(10) NOT NULL,
  `description` text NOT NULL,
  `duree` float NOT NULL,
  `budget` float NOT NULL,
  `niveaudifficulte` int(11) NOT NULL,
  `lieu` varchar(100) NOT NULL,
  `agemin` int(11) NOT NULL,
  `statut` varchar(100) NOT NULL,
  `id_cat` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `budget`
--

CREATE TABLE `budget` (
  `id_budget` int(11) NOT NULL,
  `montant_total` double(10,2) NOT NULL,
  `devise_budget` varchar(3) DEFAULT 'EUR',
  `statut_budget` varchar(20) DEFAULT 'ACTIF',
  `description_budget` text DEFAULT NULL,
  `id` int(11) NOT NULL,
  `id_voyage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id_cat` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `type` varchar(100) NOT NULL,
  `saison` varchar(100) NOT NULL,
  `niveauintensite` varchar(100) NOT NULL,
  `publiccible` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `depense`
--

CREATE TABLE `depense` (
  `id_depense` int(20) NOT NULL,
  `montant_depense` double(10,2) NOT NULL,
  `libelle_depense` varchar(100) NOT NULL,
  `categorie_depense` varchar(50) NOT NULL,
  `description_depense` text NOT NULL,
  `devise_depense` varchar(3) DEFAULT 'EUR',
  `type_paiement` varchar(30) NOT NULL,
  `date_creation` date NOT NULL,
  `id_budget` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `destination`
--

CREATE TABLE `destination` (
  `id_destination` int(11) NOT NULL,
  `nom_destination` varchar(30) NOT NULL,
  `pays_destination` varchar(30) NOT NULL,
  `description_destination` text DEFAULT NULL,
  `climat_destination` varchar(40) DEFAULT NULL,
  `saison_destination` varchar(40) DEFAULT NULL,
  `latitude_destination` double DEFAULT NULL,
  `longitude_destination` double DEFAULT NULL,
  `score_destination` double DEFAULT NULL,
  `currency_destination` varchar(255) DEFAULT NULL,
  `flag_destination` varchar(500) DEFAULT NULL,
  `languages_destination` varchar(255) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `destination`
--

INSERT INTO `destination` (`id_destination`, `nom_destination`, `pays_destination`, `description_destination`, `climat_destination`, `saison_destination`, `latitude_destination`, `longitude_destination`, `score_destination`, `currency_destination`, `flag_destination`, `languages_destination`, `video_url`) VALUES
(1, 'Nice', 'France', 'Nice, France, is a vibrant Mediterranean jewel boasting a rich artistic heritage, stunning coastline, and a lively atmosphere. Explore the Promenade des Anglais, delve into the charming Old Town (Vieux Nice), and immerse yourself in the city’s blend of French elegance and Italian flair.\n\n\n\nDay 1: Arrival & Old Town Exploration – Settle into your hotel and begin your Nice adventure with a wander through Vieux Nice. Get lost in the narrow, colorful streets, browse the Cours Saleya flower market, and enjoy a traditional Niçoise lunch.\n\nDay 2: Promenade & Castle – Stroll along the iconic Promenade des Anglais, taking in the sea views and people-watching. In the afternoon, ascend to Castle Hill (Colline du Château) for panoramic vistas of the city and coastline – perfect for sunset.\n\nDay 3: Musée Matisse & Cimiez – Immerse yourself in the world of Henri Matisse at the Musée Matisse, housed in his former villa. Afterwards, explore the Cimiez district, home to the Roman ruins, the Parc de la Colline du Château, and the Orthodox Cathedral.\n\nDay 4: Day Trip to Èze & Monaco – Take a day trip to the medieval village of Èze, perched high on a hilltop with breathtaking views. Continue to Monaco, exploring the opulent Monte Carlo Casino and the Prince’s Palace.\n\nDay 5: Beach Day & Port Lympia – Relax and soak up the sun on one of Nice’s beautiful beaches. In the afternoon, head to Port Lympia, Nice’s harbor, and enjoy a seafood dinner overlooking the boats.\n\nDay 6: Musée Marc Chagall & Saint-Paul de Vence – Visit the Musée Marc Chagall, dedicated to the works of the renowned artist who spent much of his life in Nice. Afterwards, journey to the picturesque hilltop village of Saint-Paul de Vence, known for its art galleries and stunning views.\n\nDay 7: Market & Departure – Enjoy a final morning exploring the local markets for souvenirs and treats. Depending on your flight schedule, you might have time for a last stroll along the Promenade or a final café au lait before heading to the airport.', 'Tropical', 'Été', 43.701944444, 7.268333333, 0, 'EUR (€)', 'https://flagcdn.com/w320/fr.png', 'French', 'https://www.youtube.com/watch?v=jyIux-2o69Y'),
(2, 'Rome', 'Italy', 'Historic capital with ancient monuments and vibrant culture.', 'Mediterranean', 'Spring', 41.9028, 12.4964, 4.6, 'Euro', 'https://flagcdn.com/w320/it.png', 'Italian', 'https://example.com/rome.mp4'),
(3, 'Paris', 'France', 'City of lights known for art, fashion and gastronomy.', 'Oceanic', 'Summer', 48.8566, 2.3522, 4.8, 'Euro', 'https://flagcdn.com/w320/fr.png', 'French', 'https://example.com/paris.mp4'),
(4, 'Tokyo', 'Japan', 'Modern metropolis blending tradition and technology.', 'Humid Subtropical', 'Autumn', 35.6762, 139.6503, 4.7, 'Yen', 'https://flagcdn.com/w320/jp.png', 'Japanese', 'https://example.com/tokyo.mp4');

-- --------------------------------------------------------

--
-- Table structure for table `etape`
--

CREATE TABLE `etape` (
  `id_etape` int(11) NOT NULL,
  `heure` time NOT NULL,
  `lieu` varchar(10) NOT NULL,
  `description_etape` text NOT NULL,
  `id_activite` int(11) NOT NULL,
  `id_itineraire` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `hebergement`
--

CREATE TABLE `hebergement` (
  `id_hebergement` int(11) NOT NULL,
  `nom_hebergement` varchar(100) NOT NULL,
  `type_hebergement` varchar(50) DEFAULT NULL,
  `prixNuit_hebergement` double DEFAULT NULL,
  `adresse_hebergement` varchar(100) DEFAULT NULL,
  `note_hebergement` double DEFAULT NULL,
  `latitude_hebergement` double DEFAULT NULL,
  `longitude_hebergement` double DEFAULT NULL,
  `destination_hebergement` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `hebergement`
--

INSERT INTO `hebergement` (`id_hebergement`, `nom_hebergement`, `type_hebergement`, `prixNuit_hebergement`, `adresse_hebergement`, `note_hebergement`, `latitude_hebergement`, `longitude_hebergement`, `destination_hebergement`) VALUES
(2, 'Hôtel Locarno', 'Hôtel', 125, 'Hôtel Locarno, 4 Avenue des Baumettes, 06000 Nice, France', 3, 43.69472300054007, 7.251776289308175, 1),
(3, 'Hôtel Negresco', 'Hôtel', 175, 'Hôtel Negresco, Rue du Commandant Berretta, 06000 Nice, France', 5, 43.69470965054007, 7.257752071044015, 1),
(4, 'Goldstar Suites', 'Hôtel', 150, 'Goldstar Suites, Passage Meyerbeer, 06000 Nice, France', 4, 43.69753050054002, 7.260538197109449, 1),
(5, 'Splendid Hôtel & Spa Nice', 'Hôtel', 150, 'Splendid Hôtel & Spa Nice, Rue Gounod, 06000 Nice, France', 4, 43.69875250054001, 7.259834457727271, 1),
(6, 'Le Méridien Nice', 'Hôtel', 150, 'Le Méridien Nice, Avenue Gustave V, 06000 Nice, France', 4, 43.695280300540055, 7.265958300000001, 1),
(7, 'Colosseum Hotel', 'Hotel', 120, 'Via Roma 1', 4.3, 41.903, 12.495, 2),
(8, 'Vatican Stay', 'Hotel', 150, 'Via Vaticano 10', 4.5, 41.9022, 12.4539, 2),
(9, 'Eiffel View Hotel', 'Hotel', 200, 'Avenue Gustave Eiffel', 4.7, 48.8584, 2.2945, 3),
(10, 'Montmartre Lodge', 'Hotel', 110, 'Montmartre Street 5', 4.2, 48.8867, 2.3431, 3),
(11, 'Shinjuku Grand', 'Hotel', 180, 'Shinjuku District', 4.6, 35.6938, 139.7034, 4),
(12, 'Asakusa Ryokan', 'Ryokan', 90, 'Asakusa 2-3-1', 4.4, 35.7148, 139.7967, 4);

-- --------------------------------------------------------

--
-- Table structure for table `itineraire`
--

CREATE TABLE `itineraire` (
  `id_itineraire` int(11) NOT NULL,
  `nom_iteneraire` varchar(10) NOT NULL,
  `description_iteneraire` text NOT NULL,
  `id_voyage` int(11) NOT NULL,
  `nombre_jour` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `liste_activite`
--

CREATE TABLE `liste_activite` (
  `id_voyage` int(11) NOT NULL,
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `participation`
--

CREATE TABLE `participation` (
  `id_participation` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `role_participation` varchar(10) NOT NULL,
  `id_voyage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `date_naissance` date NOT NULL,
  `email` varchar(150) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
  `photo_url` varchar(500) DEFAULT NULL,
  `verification_code` varchar(6) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `last_login_ip` varchar(45) DEFAULT NULL,
  `last_login_location` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `photo_file_name` varchar(255) DEFAULT NULL,
  `face_embedding` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `nom`, `prenom`, `date_naissance`, `email`, `telephone`, `mot_de_passe`, `role`, `photo_url`, `verification_code`, `is_verified`, `last_login_ip`, `last_login_location`, `created_at`, `photo_file_name`, `face_embedding`) VALUES
(1, 'Chekir', 'Neyrouz', '2005-01-29', 'neyrouzchekir01@gmail.com', '+21629074810', '$2a$12$HZAHY7Nb1zbudlwU28ienuKGjzKDUlPPVw9wBOdEcCFZVbLLhN6Je', 'ADMIN', NULL, NULL, 1, '102.173.115.26', 'Tunis, Tunisia, ', '2026-02-26 18:12:29', NULL, NULL),
(2, 'Boutaieb', 'Yosr', '2003-05-30', 'yosr.boutaieb@esprit.tn', NULL, '$2a$12$K52nqVrAtvx8EMHTyi7kFO1VFax0SQ5nlsExmunmWbG4h3JsKJ4YO', 'USER', NULL, NULL, 0, NULL, NULL, '2026-02-26 18:16:36', NULL, NULL),
(3, 'Chekiir', 'Neyrouzz', '2003-02-08', 'neyrouz.chekir@esprit.tn', NULL, '$2a$12$xru.KeGJexFAPNBrxxdWk.20aMGwuZ/fZmdMfy/YttCSSCuVHtMva', 'USER', NULL, NULL, 1, '102.173.115.26', 'Tunis, Tunisia, ', '2026-02-26 18:40:58', NULL, NULL),
(4, 'Boutaieb', 'Yosr', '2004-02-13', 'neyrouzchekir2005@gmail.com', NULL, '$2a$12$/KPn2qOG5U5wOBL7Wvr/XuPag3EvRbAJPivloQ1OKj8yQzi3B9Y/O', 'USER', NULL, NULL, 1, '102.173.115.26', 'Tunis, Tunisia, ', '2026-02-26 19:00:33', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `voyage`
--

CREATE TABLE `voyage` (
  `id_voyage` int(11) NOT NULL,
  `titre_voyage` varchar(10) NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `statut` varchar(10) NOT NULL,
  `id_destination` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activites`
--
ALTER TABLE `activites`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_activites_categories` (`id_cat`);

--
-- Indexes for table `budget`
--
ALTER TABLE `budget`
  ADD PRIMARY KEY (`id_budget`),
  ADD KEY `fk_budget_user` (`id`),
  ADD KEY `fk_budget_voyage` (`id_voyage`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id_cat`);

--
-- Indexes for table `depense`
--
ALTER TABLE `depense`
  ADD PRIMARY KEY (`id_depense`),
  ADD KEY `depense_fk` (`id_budget`);

--
-- Indexes for table `destination`
--
ALTER TABLE `destination`
  ADD PRIMARY KEY (`id_destination`);

--
-- Indexes for table `etape`
--
ALTER TABLE `etape`
  ADD PRIMARY KEY (`id_etape`),
  ADD KEY `fk_etape_itineraire` (`id_itineraire`),
  ADD KEY `fk_etape_activite` (`id_activite`);

--
-- Indexes for table `hebergement`
--
ALTER TABLE `hebergement`
  ADD PRIMARY KEY (`id_hebergement`),
  ADD KEY `hebergement_fk` (`destination_hebergement`);

--
-- Indexes for table `itineraire`
--
ALTER TABLE `itineraire`
  ADD PRIMARY KEY (`id_itineraire`),
  ADD KEY `fk_itineraire_voyage` (`id_voyage`);

--
-- Indexes for table `liste_activite`
--
ALTER TABLE `liste_activite`
  ADD KEY `fk_liste_activite_voyage` (`id_voyage`),
  ADD KEY `fk_liste_activite_activite` (`id`);

--
-- Indexes for table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`id_participation`),
  ADD KEY `fk_participation_voyage` (`id_voyage`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `voyage`
--
ALTER TABLE `voyage`
  ADD PRIMARY KEY (`id_voyage`),
  ADD KEY `fk_voyage_destination` (`id_destination`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activites`
--
ALTER TABLE `activites`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `budget`
--
ALTER TABLE `budget`
  MODIFY `id_budget` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id_cat` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `depense`
--
ALTER TABLE `depense`
  MODIFY `id_depense` int(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `destination`
--
ALTER TABLE `destination`
  MODIFY `id_destination` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `etape`
--
ALTER TABLE `etape`
  MODIFY `id_etape` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `hebergement`
--
ALTER TABLE `hebergement`
  MODIFY `id_hebergement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `itineraire`
--
ALTER TABLE `itineraire`
  MODIFY `id_itineraire` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `participation`
--
ALTER TABLE `participation`
  MODIFY `id_participation` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `voyage`
--
ALTER TABLE `voyage`
  MODIFY `id_voyage` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activites`
--
ALTER TABLE `activites`
  ADD CONSTRAINT `fk_activites_categories` FOREIGN KEY (`id_cat`) REFERENCES `categories` (`id_cat`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `budget`
--
ALTER TABLE `budget`
  ADD CONSTRAINT `fk_budget_user` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_budget_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `depense`
--
ALTER TABLE `depense`
  ADD CONSTRAINT `depense_fk` FOREIGN KEY (`id_budget`) REFERENCES `budget` (`id_budget`) ON DELETE CASCADE;

--
-- Constraints for table `etape`
--
ALTER TABLE `etape`
  ADD CONSTRAINT `fk_etape_activite` FOREIGN KEY (`id_activite`) REFERENCES `activites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_etape_itineraire` FOREIGN KEY (`id_itineraire`) REFERENCES `itineraire` (`id_itineraire`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `hebergement`
--
ALTER TABLE `hebergement`
  ADD CONSTRAINT `hebergement_fk` FOREIGN KEY (`destination_hebergement`) REFERENCES `destination` (`id_destination`);

--
-- Constraints for table `itineraire`
--
ALTER TABLE `itineraire`
  ADD CONSTRAINT `fk_itineraire_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `liste_activite`
--
ALTER TABLE `liste_activite`
  ADD CONSTRAINT `fk_liste_activite_activite` FOREIGN KEY (`id`) REFERENCES `activites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_liste_activite_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `fk_participation_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `voyage`
--
ALTER TABLE `voyage`
  ADD CONSTRAINT `fk_voyage_destination` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
