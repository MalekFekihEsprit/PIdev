-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : ven. 27 fév. 2026 à 02:19
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `travelmate`
--

-- --------------------------------------------------------

--
-- Structure de la table `activites`
--

CREATE TABLE `activites` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `budget` int(255) NOT NULL,
  `niveaudifficulte` varchar(100) NOT NULL,
  `lieu` varchar(500) DEFAULT NULL,
  `agemin` int(100) NOT NULL,
  `statut` varchar(100) NOT NULL,
  `duree` int(100) NOT NULL,
  `categorie_id` int(11) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `date_prevue` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `activites`
--

INSERT INTO `activites` (`id`, `nom`, `description`, `budget`, `niveaudifficulte`, `lieu`, `agemin`, `statut`, `duree`, `categorie_id`, `image_path`, `date_prevue`) VALUES
(6, 'soirée masquée', '⭐ Une activité exceptionnelle vous attend : soirée masquée à paris ! Niveau facile, cette expérience unique restera gravée dans vos mémoires.', 70, 'Facile', 'paris', 20, 'Active', 4, 3, 'uploads\\activites\\act_20260220_232708.jpg', '2026-02-28'),
(8, 'Randonnée', '🌈 Plongez dans l\'univers magique de Randonnée à Jbal Rsas ! Une activité de niveau difficile qui éveillera tous vos sens.', 450, 'Difficile', 'Dolomites', 15, 'Inactive', 3, 5, 'uploads\\activites\\act_20260220_232623.jpg', NULL),
(15, 'soirée disco', '🔥 L\'aventure vous appelle ! soirée disco à marsa est l\'activité parfaite pour les amateurs de sensations (niveau facile). Venez vivre des moments intenses !', 15, 'Facile', 'marsa', 18, 'Active', 2, 3, 'uploads\\activites\\act_20260224_002508.jpg', NULL),
(17, 'camping', '🎉 Découvrez camping comme vous ne l\'avez jamais vu à Sidi Bou Said, Tunisie ! Activité de niveau difficile, moments de pur bonheur !', 20, 'Difficile', 'Sidi Bou Said, Tunisie', 20, 'En attente', 1, 7, 'uploads\\activites\\act_20260222_231844.jpg', '2026-03-31'),
(20, 'camping', '🌟 Vivez une expérience unique avec camping à jendouba ! Que vous soyez débutant ou expert (niveau expert), cette activité saura vous séduire par son authenticité.', 15, 'Expert', 'jendouba', 15, 'Active', 24, 7, 'uploads\\activites\\act_20260223_235244.jpg', NULL),
(21, 'soirée halloween', '🔥 L\'aventure vous appelle ! soirée halloween à paris est l\'activité parfaite pour les amateurs de sensations (niveau facile). Venez vivre des moments intenses !', 25, 'Facile', 'paris', 18, 'Active', 5, 3, 'uploads\\activites\\act_20260224_002041.jpg', NULL),
(24, 'sortie musée', '🌟 Vivez une expérience unique avec sortie musée à Avenue Abdelaziz Kamel, El Bouhaira, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie ! Cette activité de niveau moyen saura vous séduire.', 15, 'Moyen', 'Avenue Abdelaziz Kamel, El Bouhaira, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie', 15, 'Active', 2, 13, 'uploads\\activites\\act_20260225_234011.jpg', '2026-03-04'),
(26, 'sortie vélo', '🎉 Découvrez sortie vélo comme vous ne l\'avez jamais vu à Rue 8001, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie ! Activité de niveau moyen, moments de pur bonheur !', 15, 'Moyen', 'Rue 8001, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie', 15, 'Active', 2, 5, 'uploads\\activites\\act_20260225_234826.jpg', '2026-03-04'),
(27, 'sortie musée', '🎉 Découvrez sortie musée comme vous ne l\'avez jamais vu à Tour Eiffel, 5, Avenue Anatole France, Quartier du Gros-Caillou, Paris 7e Arrondissement, Paris, Île-de-France, France métropolitaine, 75007, France ! Activité de niveau difficile, moments de pur bonheur !', 20, 'Difficile', 'Tour Eiffel, 5, Avenue Anatole France, Quartier du Gros-Caillou, Paris 7e Arrondissement, Paris, Île-de-France, France métropolitaine, 75007, France', 18, 'Active', 1, 13, 'uploads\\activites\\act_20260226_013624.jpg', '2026-03-05'),
(28, 'sortie musée', '🌟 Vivez une expérience unique avec sortie musée à Allée 1165, Notre Dame, 01 Juin, Délégation El Menzah, Tunis, Gouvernorat Tunis, 1075, Tunisie ! Cette activité de niveau facile saura vous séduire.', 30, 'Facile', 'Allée 1165, Notre Dame, 01 Juin, Délégation El Menzah, Tunis, Gouvernorat Tunis, 1075, Tunisie', 15, 'Inactive', 1, 5, 'uploads\\activites\\act_20260226_023705.jpg', '2026-03-06'),
(29, 'soirée', '✨ Préparez-vous pour une aventure inoubliable ! soirée vous attend à Jardin Japonais, Rue Abou Hamed El Ghazali, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie. Une activité de niveau facile qui vous fera vibrer !', 15, 'Facile', 'Jardin Japonais, Rue Abou Hamed El Ghazali, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie', 12, 'Active', 2, 3, 'uploads\\activites\\act_20260226_170646.jpg', '2026-03-05'),
(30, 'soirée', '✨ Préparez-vous pour une aventure inoubliable ! soirée vous attend à Institut Supérieur du Sport et de l\'Education Physique de Ksar Saïd, Rue Es-sirt, La Manouba, Ksar - Said, Délégation Manouba‬‬, Gouvernorat La Manouba, 2041, Tunisie. Une activité de niveau moyen qui vous fera vibrer !', 150, 'Moyen', 'Institut Supérieur du Sport et de l\'Education Physique de Ksar Saïd, Rue Es-sirt, La Manouba, Ksar - Said, Délégation Manouba‬‬, Gouvernorat La Manouba, 2041, Tunisie', 15, 'Active', 2, 3, 'uploads\\activites\\act_20260226_173311.jpg', '2026-03-05'),
(31, 'cours de peinture', '🎉 Découvrez cours de peinture comme vous ne l\'avez jamais vu à Boulevard Qualité de la Vie, Gammarth, Délégation La Marsa, Tunis, Gouvernorat Tunis, 1057, Tunisie ! Activité de niveau facile, moments de pur bonheur !', 20, 'Facile', 'Boulevard Qualité de la Vie, Gammarth, Délégation La Marsa, Tunis, Gouvernorat Tunis, 1057, Tunisie', 12, 'Active', 2, 17, 'uploads\\activites\\act_20260226_225545.jpg', '2026-03-05');

-- --------------------------------------------------------

--
-- Structure de la table `budget`
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

--
-- Déchargement des données de la table `budget`
--

INSERT INTO `budget` (`id_budget`, `montant_total`, `devise_budget`, `statut_budget`, `description_budget`, `id`, `id_voyage`) VALUES
(1, 1000.00, 'EUR', 'ACTIF', 'Budget voyage Nice', 1, 1),
(2, 2000.00, 'EUR', 'ACTIF', 'Budget voyage Rome', 2, 2),
(3, 3000.00, 'EUR', 'ACTIF', 'Budget voyage Paris', 3, 3);

-- --------------------------------------------------------

--
-- Structure de la table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `type` varchar(100) NOT NULL,
  `saison` varchar(100) NOT NULL,
  `niveauintensite` varchar(100) NOT NULL,
  `publiccible` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categories`
--

INSERT INTO `categories` (`id`, `nom`, `description`, `type`, `saison`, `niveauintensite`, `publiccible`) VALUES
(3, 'soirée', 'Événements nocturnes pour se divertir, rencontrer du monde et profiter d’ambiances festives.', 'Détente', 'Toutes saisons', 'Moyen', 'adulte'),
(5, 'randonnée', 'Activités en plein air permettant d’explorer la nature à pied, sur des sentiers adaptés à différents niveaux.', 'Aventure', 'Printemps', 'Moyen', 'adulte'),
(7, 'camping', 'Découvrir les activtés \"camping\" que offrent notre application et laissez la nature vous faire rever', 'Aventure', 'Été', 'Élevé', 'adulte'),
(12, 'shopping', 'Activités centrées sur la découverte de boutiques, centres commerciaux et marchés pour faire des achats.', 'Détente', 'Toutes saisons', 'Faible', 'femme'),
(13, 'musée', 'Activités dédiés à la découverte du patrimoine, de l’histoire, de la science ou de l’art à travers des collections et expositions.', 'Culturel', 'Été', 'Faible', 'adulte'),
(17, 'Art', 'Expériences artistiques liées à la créativité comme expositions, ateliers artistiques ou événements culturels.', 'Détente', 'Toutes saisons', 'Faible', 'tout le monde'),
(18, 'sorties conviviales', 'Moments de détente autour de la gastronomie et des boissons dans des lieux conviviaux.', 'Gastronomique', 'Toutes saisons', 'Faible', 'adulte');

-- --------------------------------------------------------

--
-- Structure de la table `depense`
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

--
-- Déchargement des données de la table `depense`
--

INSERT INTO `depense` (`id_depense`, `montant_depense`, `libelle_depense`, `categorie_depense`, `description_depense`, `devise_depense`, `type_paiement`, `date_creation`, `id_budget`) VALUES
(1, 150.00, 'Hôtel Nice', 'Hébergement', 'Nuitée à l\'hôtel à Nice', 'EUR', 'Carte', '2026-02-26', 1),
(2, 45.00, 'Restaurant', 'Restauration', 'Dîner au restaurant', 'EUR', 'Espèces', '2026-02-26', 1);

-- --------------------------------------------------------

--
-- Structure de la table `destination`
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
-- Déchargement des données de la table `destination`
--

INSERT INTO `destination` (`id_destination`, `nom_destination`, `pays_destination`, `description_destination`, `climat_destination`, `saison_destination`, `latitude_destination`, `longitude_destination`, `score_destination`, `currency_destination`, `flag_destination`, `languages_destination`, `video_url`) VALUES
(1, 'Nice', 'France', 'Nice, France, is a vibrant Mediterranean jewel...', 'Tropical', 'Été', 43.701944444, 7.268333333, 0, 'EUR (€)', 'https://flagcdn.com/w320/fr.png', 'French', 'https://www.youtube.com/watch?v=jyIux-2o69Y'),
(2, 'Rome', 'Italy', 'Historic capital with ancient monuments and vibrant culture.', 'Mediterranean', 'Spring', 41.9028, 12.4964, 4.6, 'Euro', 'https://flagcdn.com/w320/it.png', 'Italian', 'https://example.com/rome.mp4'),
(3, 'Paris', 'France', 'City of lights known for art, fashion and gastronomy.', 'Oceanic', 'Summer', 48.8566, 2.3522, 4.8, 'Euro', 'https://flagcdn.com/w320/fr.png', 'French', 'https://example.com/paris.mp4'),
(4, 'Tokyo', 'Japan', 'Modern metropolis blending tradition and technology.', 'Humid Subtropical', 'Autumn', 35.6762, 139.6503, 4.7, 'Yen', 'https://flagcdn.com/w320/jp.png', 'Japanese', 'https://example.com/tokyo.mp4');

-- --------------------------------------------------------

--
-- Structure de la table `etape`
--

CREATE TABLE `etape` (
  `id_etape` int(11) NOT NULL,
  `heure` time NOT NULL,
  `lieu` varchar(255) NOT NULL,
  `description_etape` text NOT NULL,
  `id_activite` int(11) NOT NULL,
  `id_itineraire` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `hebergement`
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
-- Déchargement des données de la table `hebergement`
--

INSERT INTO `hebergement` (`id_hebergement`, `nom_hebergement`, `type_hebergement`, `prixNuit_hebergement`, `adresse_hebergement`, `note_hebergement`, `latitude_hebergement`, `longitude_hebergement`, `destination_hebergement`) VALUES
(1, 'Hôtel Locarno', 'Hôtel', 125, '4 Avenue des Baumettes, 06000 Nice, France', 3, 43.69472300054007, 7.251776289308175, 1),
(2, 'Hôtel Negresco', 'Hôtel', 175, 'Rue du Commandant Berretta, 06000 Nice, France', 5, 43.69470965054007, 7.257752071044015, 1),
(3, 'Goldstar Suites', 'Hôtel', 150, 'Passage Meyerbeer, 06000 Nice, France', 4, 43.69753050054002, 7.260538197109449, 1),
(4, 'Splendid Hôtel & Spa Nice', 'Hôtel', 150, 'Rue Gounod, 06000 Nice, France', 4, 43.69875250054001, 7.259834457727271, 1),
(5, 'Le Méridien Nice', 'Hôtel', 150, 'Avenue Gustave V, 06000 Nice, France', 4, 43.695280300540055, 7.265958300000001, 1),
(6, 'Colosseum Hotel', 'Hotel', 120, 'Via Roma 1, Rome', 4.3, 41.903, 12.495, 2),
(7, 'Vatican Stay', 'Hotel', 150, 'Via Vaticano 10, Rome', 4.5, 41.9022, 12.4539, 2),
(8, 'Eiffel View Hotel', 'Hotel', 200, 'Avenue Gustave Eiffel, Paris', 4.7, 48.8584, 2.2945, 3),
(9, 'Montmartre Lodge', 'Hotel', 110, 'Montmartre Street 5, Paris', 4.2, 48.8867, 2.3431, 3),
(10, 'Shinjuku Grand', 'Hotel', 180, 'Shinjuku District, Tokyo', 4.6, 35.6938, 139.7034, 4),
(11, 'Asakusa Ryokan', 'Ryokan', 90, 'Asakusa 2-3-1, Tokyo', 4.4, 35.7148, 139.7967, 4);

-- --------------------------------------------------------

--
-- Structure de la table `itineraire`
--

CREATE TABLE `itineraire` (
  `id_itineraire` int(11) NOT NULL,
  `nom_iteneraire` varchar(100) NOT NULL,
  `description_iteneraire` text NOT NULL,
  `id_voyage` int(11) NOT NULL,
  `nombre_jour` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `liste_activite`
--

CREATE TABLE `liste_activite` (
  `id_voyage` int(11) NOT NULL,
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `participation`
--

CREATE TABLE `participation` (
  `id_participation` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `role_participation` varchar(50) NOT NULL,
  `id_voyage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user`
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
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `nom`, `prenom`, `date_naissance`, `email`, `telephone`, `mot_de_passe`, `role`, `photo_url`, `verification_code`, `is_verified`, `last_login_ip`, `last_login_location`, `created_at`, `photo_file_name`, `face_embedding`) VALUES
(1, 'Chekir', 'Neyrouz', '2005-01-29', 'neyrouzchekir01@gmail.com', '+21629074810', '$2a$12$HZAHY7Nb1zbudlwU28ienuKGjzKDUlPPVw9wBOdEcCFZVbLLhN6Je', 'ADMIN', NULL, NULL, 1, '102.173.115.26', 'Tunis, Tunisia', '2026-02-26 18:12:29', NULL, NULL),
(2, 'Boutaieb', 'Yosr', '2003-05-30', 'yosr.boutaieb@esprit.tn', NULL, '$2a$12$K52nqVrAtvx8EMHTyi7kFO1VFax0SQ5nlsExmunmWbG4h3JsKJ4YO', 'USER', NULL, NULL, 0, NULL, NULL, '2026-02-26 18:16:36', NULL, NULL),
(3, 'Chekir', 'Neyrouz', '2003-02-08', 'neyrouz.chekir@esprit.tn', NULL, '$2a$12$xru.KeGJexFAPNBrxxdWk.20aMGwuZ/fZmdMfy/YttCSSCuVHtMva', 'USER', NULL, NULL, 1, '102.173.115.26', 'Tunis, Tunisia', '2026-02-26 18:40:58', NULL, NULL),
(4, 'Boutaieb', 'Yosr', '2004-02-13', 'neyrouzchekir2005@gmail.com', NULL, '$2a$12$/KPn2qOG5U5wOBL7Wvr/XuPag3EvRbAJPivloQ1OKj8yQzi3B9Y/O', 'USER', NULL, NULL, 1, '102.173.115.26', 'Tunis, Tunisia', '2026-02-26 19:00:33', NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `voyage`
--

CREATE TABLE `voyage` (
  `id_voyage` int(11) NOT NULL,
  `titre_voyage` varchar(100) NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `statut` varchar(50) NOT NULL,
  `id_destination` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `voyage`
--

INSERT INTO `voyage` (`id_voyage`, `titre_voyage`, `date_debut`, `date_fin`, `statut`, `id_destination`) VALUES
(1, 'Voyage Nice', '2026-03-01 00:00:00', '2026-03-07 00:00:00', 'Prévu', 1),
(2, 'Découverte Rome', '2026-04-10 00:00:00', '2026-04-15 00:00:00', 'Prévu', 2),
(3, 'Séjour Paris', '2026-05-05 00:00:00', '2026-05-10 00:00:00', 'Prévu', 3);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `activites`
--
ALTER TABLE `activites`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_activites_categories` (`categorie_id`);

--
-- Index pour la table `budget`
--
ALTER TABLE `budget`
  ADD PRIMARY KEY (`id_budget`),
  ADD KEY `fk_budget_user` (`id`),
  ADD KEY `fk_budget_voyage` (`id_voyage`);

--
-- Index pour la table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `depense`
--
ALTER TABLE `depense`
  ADD PRIMARY KEY (`id_depense`),
  ADD KEY `depense_fk` (`id_budget`);

--
-- Index pour la table `destination`
--
ALTER TABLE `destination`
  ADD PRIMARY KEY (`id_destination`);

--
-- Index pour la table `etape`
--
ALTER TABLE `etape`
  ADD PRIMARY KEY (`id_etape`),
  ADD KEY `fk_etape_itineraire` (`id_itineraire`),
  ADD KEY `fk_etape_activite` (`id_activite`);

--
-- Index pour la table `hebergement`
--
ALTER TABLE `hebergement`
  ADD PRIMARY KEY (`id_hebergement`),
  ADD KEY `hebergement_fk` (`destination_hebergement`);

--
-- Index pour la table `itineraire`
--
ALTER TABLE `itineraire`
  ADD PRIMARY KEY (`id_itineraire`),
  ADD KEY `fk_itineraire_voyage` (`id_voyage`);

--
-- Index pour la table `liste_activite`
--
ALTER TABLE `liste_activite`
  ADD KEY `fk_liste_activite_voyage` (`id_voyage`),
  ADD KEY `fk_liste_activite_activite` (`id`);

--
-- Index pour la table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`id_participation`),
  ADD KEY `fk_participation_user` (`id`),
  ADD KEY `fk_participation_voyage` (`id_voyage`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Index pour la table `voyage`
--
ALTER TABLE `voyage`
  ADD PRIMARY KEY (`id_voyage`),
  ADD KEY `fk_voyage_destination` (`id_destination`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `activites`
--
ALTER TABLE `activites`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT pour la table `budget`
--
ALTER TABLE `budget`
  MODIFY `id_budget` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `depense`
--
ALTER TABLE `depense`
  MODIFY `id_depense` int(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `destination`
--
ALTER TABLE `destination`
  MODIFY `id_destination` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `etape`
--
ALTER TABLE `etape`
  MODIFY `id_etape` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `hebergement`
--
ALTER TABLE `hebergement`
  MODIFY `id_hebergement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT pour la table `itineraire`
--
ALTER TABLE `itineraire`
  MODIFY `id_itineraire` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `participation`
--
ALTER TABLE `participation`
  MODIFY `id_participation` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `voyage`
--
ALTER TABLE `voyage`
  MODIFY `id_voyage` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `activites`
--
ALTER TABLE `activites`
  ADD CONSTRAINT `fk_activites_categories` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Contraintes pour la table `budget`
--
ALTER TABLE `budget`
  ADD CONSTRAINT `fk_budget_user` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_budget_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `depense`
--
ALTER TABLE `depense`
  ADD CONSTRAINT `depense_fk` FOREIGN KEY (`id_budget`) REFERENCES `budget` (`id_budget`) ON DELETE CASCADE;

--
-- Contraintes pour la table `etape`
--
ALTER TABLE `etape`
  ADD CONSTRAINT `fk_etape_activite` FOREIGN KEY (`id_activite`) REFERENCES `activites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_etape_itineraire` FOREIGN KEY (`id_itineraire`) REFERENCES `itineraire` (`id_itineraire`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `hebergement`
--
ALTER TABLE `hebergement`
  ADD CONSTRAINT `hebergement_fk` FOREIGN KEY (`destination_hebergement`) REFERENCES `destination` (`id_destination`) ON DELETE CASCADE;

--
-- Contraintes pour la table `itineraire`
--
ALTER TABLE `itineraire`
  ADD CONSTRAINT `fk_itineraire_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `liste_activite`
--
ALTER TABLE `liste_activite`
  ADD CONSTRAINT `fk_liste_activite_activite` FOREIGN KEY (`id`) REFERENCES `activites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_liste_activite_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `fk_participation_user` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_participation_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `voyage`
--
ALTER TABLE `voyage`
  ADD CONSTRAINT `fk_voyage_destination` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`) ON DELETE CASCADE ON UPDATE CASCADE;
SET FOREIGN_KEY_CHECKS=1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
